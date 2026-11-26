package eg.gov.health.platform.dashboard;
import eg.gov.health.platform.adt.*;
import eg.gov.health.platform.clinical.EncounterRepository;
import eg.gov.health.platform.cycles.CycleInstanceRepository;
import eg.gov.health.platform.emergency.AmbulanceUnitRepository;
import eg.gov.health.platform.facility.*;
import eg.gov.health.platform.patient.PatientRepository;
import org.springframework.stereotype.Component;
import java.time.*; import java.util.*; import java.util.stream.Collectors;
/** Real aggregate computation shared by the authenticated dashboard
 *  (/api/dashboard) and the anonymous public homepage (/api/public).
 *  Scope is explicit: a facility id, a governorate, or national (both
 *  null). RLS additionally constrains patient/encounter/cycle reads to
 *  the caller's tenant setting, so a facility user can never aggregate
 *  beyond their own scope even here. Counts only — never patient rows. */
@Component
public class Aggregates {
  private final FacilityRepository facilities; private final PatientRepository patients;
  private final EncounterRepository encounters; private final AmbulanceUnitRepository ambulances;
  private final CycleInstanceRepository cycles; private final WardRepository wards;
  private final InpatientAdmissionRepository admissions;
  public Aggregates(FacilityRepository facilities, PatientRepository patients, EncounterRepository encounters,
                    AmbulanceUnitRepository ambulances, CycleInstanceRepository cycles,
                    WardRepository wards, InpatientAdmissionRepository admissions) {
    this.facilities=facilities; this.patients=patients; this.encounters=encounters; this.ambulances=ambulances;
    this.cycles=cycles; this.wards=wards; this.admissions=admissions;
  }
  public record Scope(UUID facilityId, String governorate) {
    public static Scope national() { return new Scope(null, null); }
    public boolean test(UUID fId, String gov) {
      if (facilityId != null) return facilityId.equals(fId);
      if (governorate != null) return governorate.equals(gov);
      return true;
    }
  }
  private static final ZoneId CAIRO = ZoneId.of("Africa/Cairo");
  private static boolean today(OffsetDateTime ts) {
    return ts != null && ts.atZoneSameInstant(CAIRO).toLocalDate().equals(LocalDate.now(CAIRO));
  }
  private static double pct(long part, long total) {
    return total == 0 ? 0.0 : Math.round(1000.0 * part / total) / 10.0;
  }

  public Map<String, Object> kpis(Scope scope) {
    var facs = facilities.findAll().stream()
      .filter(f -> "active".equals(f.getStatus()) && scope.test(f.getId(), f.getGovernorate())).toList();
    long patientsCount = patients.findAll().stream().filter(p -> scope.test(p.getFacilityId(), p.getGovernorate())).count();
    var encs = encounters.findAll();
    long encountersToday = encs.stream().filter(e -> today(e.getArrivedAt())).count();
    var activeWards = wards.findByActiveTrue().stream().filter(w -> scope.test(w.getFacilityId(), w.getGovernorate())).toList();
    var activeAdmissions = admissions.findByStatus("admitted").stream()
      .filter(a -> scope.test(a.getFacilityId(), a.getGovernorate())).toList();
    long totalWardBeds = activeWards.stream().mapToLong(Ward::getTotalBeds).sum();
    long occupied = activeAdmissions.size();
    var icuWardIds = activeWards.stream().filter(w -> "icu".equals(w.getWardType())).map(Ward::getId).collect(Collectors.toSet());
    long icuBeds = activeWards.stream().filter(w -> "icu".equals(w.getWardType())).mapToLong(Ward::getTotalBeds).sum();
    long icuOccupied = activeAdmissions.stream().filter(a -> icuWardIds.contains(a.getWardId())).count();
    long ambulancesAvailable = ambulances.findByStatus("available").stream()
      .filter(u -> scope.test(u.getFacilityId(), u.getGovernorate())).count();
    var now = OffsetDateTime.now();
    var openCycles = cycles.findAll().stream()
      .filter(c -> !List.of("completed", "closed", "cancelled").contains(c.getStatus())).toList();
    long breaches = openCycles.stream().filter(c -> c.getSlaDueAt() != null && c.getSlaDueAt().isBefore(now)).count();
    var m = new LinkedHashMap<String, Object>();
    m.put("facilitiesActive", facs.size());
    m.put("patientsRegistered", patientsCount);
    m.put("encountersToday", encountersToday);
    m.put("bedOccupancyPct", pct(occupied, totalWardBeds));
    m.put("icuOccupancyPct", pct(icuOccupied, icuBeds));
    m.put("ambulancesAvailable", ambulancesAvailable);
    m.put("openCycleInstances", openCycles.size());
    m.put("slaBreaches", breaches);
    return m;
  }

  public List<Map<String, Object>> capacityByGovernorate(Scope scope) {
    var byGov = new TreeMap<String, Map<String, Object>>();
    for (var f : facilities.findAll()) {
      if (!"active".equals(f.getStatus()) || !scope.test(f.getId(), f.getGovernorate())) continue;
      var row = byGov.computeIfAbsent(f.getGovernorate() == null ? "—" : f.getGovernorate(), g -> {
        var r = new LinkedHashMap<String, Object>();
        r.put("governorate", g); r.put("facilities", 0); r.put("totalBeds", 0);
        r.put("occupiedBeds", 0); r.put("icuOccupied", 0); r.put("icuBeds", 0); r.put("ambulancesAvailable", 0);
        return r;
      });
      row.put("facilities", (Integer) row.get("facilities") + 1);
      row.put("totalBeds", (Integer) row.get("totalBeds") + (f.getBedCapacity() == null ? 0 : f.getBedCapacity()));
    }
    var facilityGov = new HashMap<UUID, String>();
    facilities.findAll().forEach(f -> facilityGov.put(f.getId(), f.getGovernorate()));
    for (var a : admissions.findByStatus("admitted")) {
      var row = byGov.get(facilityGov.getOrDefault(a.getFacilityId(), "—"));
      if (row != null) row.put("occupiedBeds", (Integer) row.get("occupiedBeds") + 1);
    }
    var icuWards = new HashMap<UUID, String>();
    for (var w : wards.findByActiveTrue()) {
      if (!"icu".equals(w.getWardType())) continue;
      icuWards.put(w.getId(), facilityGov.get(w.getFacilityId()));
      var row = byGov.get(facilityGov.getOrDefault(w.getFacilityId(), "—"));
      if (row != null) row.put("icuBeds", (Integer) row.get("icuBeds") + w.getTotalBeds());
    }
    for (var a : admissions.findByStatus("admitted")) {
      var gov = icuWards.get(a.getWardId());
      if (gov != null && byGov.containsKey(gov)) {
        var row = byGov.get(gov);
        row.put("icuOccupied", (Integer) row.get("icuOccupied") + 1);
      }
    }
    for (var u : ambulances.findByStatus("available")) {
      var row = byGov.get(u.getGovernorate() == null ? "—" : u.getGovernorate());
      if (row != null) row.put("ambulancesAvailable", (Integer) row.get("ambulancesAvailable") + 1);
    }
    return new ArrayList<>(byGov.values());
  }

  /** Public facility directory: identity + declared capacity + live
   *  inpatient census and ER waiting for each active facility. */
  public List<Map<String, Object>> facilityDirectory(Scope scope) {
    var census = new HashMap<UUID, Long>();
    admissions.findByStatus("admitted").forEach(a -> census.merge(a.getFacilityId(), 1L, Long::sum));
    var wardBeds = new HashMap<UUID, Long>();
    wards.findByActiveTrue().forEach(w -> wardBeds.merge(w.getFacilityId(), (long) w.getTotalBeds(), Long::sum));
    var erWaiting = new HashMap<UUID, Long>();
    encounters.findAll().stream()
      .filter(e -> "emergency".equals(e.getType()) && "open".equals(e.getStatus()) && today(e.getArrivedAt()))
      .forEach(e -> erWaiting.merge(e.getFacilityId(), 1L, Long::sum));
    var rows = new ArrayList<Map<String, Object>>();
    for (var f : facilities.findAll()) {
      if (!"active".equals(f.getStatus()) || !scope.test(f.getId(), f.getGovernorate())) continue;
      long inpatients = census.getOrDefault(f.getId(), 0L);
      long beds = wardBeds.getOrDefault(f.getId(), 0L);
      var m = new LinkedHashMap<String, Object>();
      m.put("id", f.getId()); m.put("nameAr", f.getNameAr()); m.put("nameEn", f.getNameEn());
      m.put("type", f.getType()); m.put("level", f.getLevel()); m.put("governorate", f.getGovernorate());
      m.put("directorate", f.getDirectorate()); m.put("city", f.getCity()); m.put("ownership", f.getOwnership());
      m.put("bedCapacity", f.getBedCapacity());
      m.put("wardBeds", beds); m.put("inpatients", inpatients);
      m.put("occupancyPct", beds == 0 ? null : pct(inpatients, beds));
      m.put("erWaiting", erWaiting.getOrDefault(f.getId(), 0L));
      rows.add(m);
    }
    rows.sort(Comparator.comparing(r -> String.valueOf(r.get("nameAr"))));
    return rows;
  }
}
