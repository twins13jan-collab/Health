package eg.gov.health.platform.cssd;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Central sterilization & instrument reprocessing (module 105).
 *  Contaminated sets are received and sorted; the reprocessing chain
 *  (clean, disinfect, dry, inspect, pack, sterilize) is documented
 *  per set with sterilizer and cycle identity and chemical/biological
 *  verification indicators. Release needs the complete chain and a
 *  passed chemical indicator; implant loads also need a PASSED
 *  biological indicator. Failures quarantine the load, and a failure
 *  found after release recalls the affected sets. Sterile storage
 *  carries expiry; expired sets are never issued; every use links
 *  the set to its procedure and patient. */
@RestController @RequestMapping("/api/cssd")
public class CssdController {
  private final InstrumentSetRepository sets; private final CssdReprocessingCycleRepository cycles;
  private final SterilizerRecordRepository sterilizers; private final SetUsageLinkRepository usage;
  private final SterilizationFailureRepository failures; private final AuditService audit;
  public CssdController(InstrumentSetRepository sets, CssdReprocessingCycleRepository cycles, SterilizerRecordRepository sterilizers,
                        SetUsageLinkRepository usage, SterilizationFailureRepository failures, AuditService audit) {
    this.sets=sets; this.cycles=cycles; this.sterilizers=sterilizers; this.usage=usage; this.failures=failures; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private InstrumentSet set(UUID id) {
    return sets.findById(id).orElseThrow(() -> BizException.notFound("set_not_found","طقم الأدوات غير موجود"));
  }
  private ReprocessingCycle cycle(UUID id) {
    return cycles.findById(id).orElseThrow(() -> BizException.notFound("cycle_not_found","دورة إعادة المعالجة غير موجودة"));
  }

  public record SetRequest(String setCode, String name, Boolean containsImplants) {}
  @PostMapping("/sets")
  Map<String,Object> registerSet(@RequestBody SetRequest r) {
    var p = principal();
    var s = sets.save(new InstrumentSet(UUID.fromString(p.facilityId()), p.governorate(), r.setCode(), r.name(), Boolean.TRUE.equals(r.containsImplants())));
    audit.record("cssd.set.registered","instrument_set",s.getId().toString(),"{}");
    return Map.of("ok",true,"set",s);
  }
  public record ReceiveRequest(String receivedFrom, String sortedBy) {}
  @PostMapping("/sets/{id}/receive")
  Map<String,Object> receive(@PathVariable UUID id, @RequestBody ReceiveRequest r) {
    var p = principal(); var s = set(id);
    var cy = cycles.save(new ReprocessingCycle(s.getFacilityId(), p.governorate(), id, r.receivedFrom(), r.sortedBy()));
    s.setStatus("in_process"); sets.save(s);
    audit.record("cssd.set.received","reprocessing_cycle",cy.getId().toString(),"{}");
    return Map.of("ok",true,"cycle",cy);
  }
  public record StepsRequest(Boolean cleaning, Boolean disinfection, Boolean dried, Boolean inspected, String packedBy, String sterilizerCode, String cycleNo) {}
  @PostMapping("/cycles/{id}/steps")
  Map<String,Object> steps(@PathVariable UUID id, @RequestBody StepsRequest r) {
    var cy = cycle(id);
    cy.recordSteps(Boolean.TRUE.equals(r.cleaning()), Boolean.TRUE.equals(r.disinfection()), Boolean.TRUE.equals(r.dried()),
      Boolean.TRUE.equals(r.inspected()), r.packedBy(), r.sterilizerCode(), r.cycleNo());
    cycles.save(cy);
    audit.record("cssd.steps.recorded","reprocessing_cycle",id.toString(),"{}");
    return Map.of("ok",true,"cycle",cy);
  }
  @PostMapping("/cycles/{id}/indicators")
  Map<String,Object> indicators(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var cy = cycle(id);
    cy.recordIndicators(body.get("chemical"), body.get("biological")); cycles.save(cy);
    if ("fail".equals(body.get("chemical")) || "fail".equals(body.get("biological"))) {
      // A failed indicator quarantines the load immediately.
      cy.quarantine(); cycles.save(cy);
      var s = set(cy.getSetId()); s.setStatus("quarantined"); sets.save(s);
    }
    audit.record("cssd.indicators.recorded","reprocessing_cycle",id.toString(),"{}");
    return Map.of("ok",true,"cycle",cy,"quarantined","quarantined".equals(cy.getStatus()));
  }
  public record ReleaseRequest(String storageLocation, LocalDate expiryDate) {}
  @PostMapping("/cycles/{id}/release")
  Map<String,Object> release(@PathVariable UUID id, @RequestBody ReleaseRequest r) {
    var p = principal(); var cy = cycle(id); var s = set(cy.getSetId());
    boolean chain = CssdRules.chainComplete(cy.isCleaningDone(), cy.isDisinfectionDone(), cy.isDried(), cy.isInspected(), cy.getPackedBy());
    if (!CssdRules.releasable(chain, cy.getChemicalIndicator(), cy.getBiologicalIndicator(), s.isContainsImplants()))
      throw BizException.conflict("not_releasable","لا إفراج قبل سلسلة مكتملة ومؤشر كيميائي ناجح — وأحمال الغرسات تنتظر نجاح المؤشر البيولوجي");
    cy.release(p.email()); cycles.save(cy);
    s.store(r.storageLocation(), r.expiryDate()); sets.save(s);
    audit.record("cssd.cycle.released","reprocessing_cycle",id.toString(),"{}");
    return Map.of("ok",true,"cycle",cy,"set_status",s.getStatus());
  }
  public record UseRequest(String procedureRef, String patientRef) {}
  @PostMapping("/sets/{id}/use")
  Map<String,Object> use(@PathVariable UUID id, @RequestBody UseRequest r) {
    var p = principal(); var s = set(id);
    // Expired or non-sterile sets are never issued; use is traceable to procedure and patient.
    if (!CssdRules.issuable(s.getStatus(), s.getExpiryDate(), LocalDate.now()))
      throw BizException.conflict("not_issuable","طقم منتهي الصلاحية أو غير معقم لا يصرف للاستخدام أبدًا");
    if (r.procedureRef()==null || r.procedureRef().isBlank()) throw BizException.badRequest("procedure_ref","كل طقم مستخدم يرتبط بإجرائه والمريض للتتبع");
    var link = usage.save(new SetUsageLink(s.getFacilityId(), p.governorate(), id, r.procedureRef(), r.patientRef()));
    s.setStatus("issued"); sets.save(s);
    audit.record("cssd.set.used","set_usage_link",link.getId().toString(),"{}");
    return Map.of("ok",true,"usage_link",link);
  }
  public record FailureRequest(UUID cycleId, String sterilizerCode, String failureType, String affectedSets, Boolean recallIssued, String investigation) {}
  @PostMapping("/failures")
  Map<String,Object> failure(@RequestBody FailureRequest r) {
    var p = principal();
    var f = failures.save(new SterilizationFailure(UUID.fromString(p.facilityId()), p.governorate(), r.cycleId(), r.sterilizerCode(),
      r.failureType(), r.affectedSets(), Boolean.TRUE.equals(r.recallIssued()), r.investigation()));
    if (r.cycleId()!=null) {
      var cy = cycle(r.cycleId());
      if ("released".equals(cy.getStatus())) {
        // Failure discovered after release: the set is recalled, not quietly re-shelved.
        var s = set(cy.getSetId()); s.setStatus("recalled"); sets.save(s);
        cy.fail(); cycles.save(cy);
      } else { cy.quarantine(); cycles.save(cy); var s = set(cy.getSetId()); s.setStatus("quarantined"); sets.save(s); }
    }
    audit.record("cssd.failure.reported","sterilization_failure",f.getId().toString(),"{}");
    return Map.of("ok",true,"failure",f);
  }
  /** Department board (Phase 4.10): every set as an attention row —
   *  recalls and quarantines first, expired sterile stock next, then
   *  expiring stock and work in process with its missing chain steps
   *  named and its release readiness computed by the same rules the
   *  release endpoint enforces. Sterilizers carry their maintenance
   *  and calibration due states alongside. */
  @GetMapping("/board")
  Map<String,Object> board() {
    var p = principal(); UUID fac = UUID.fromString(p.facilityId());
    var today = LocalDate.now(ZoneId.of("Africa/Cairo"));
    var now = OffsetDateTime.now();
    var facCycles = cycles.findByFacilityIdOrderByCreatedAtDesc(fac);
    Map<UUID, ReprocessingCycle> activeCycle = new HashMap<>();
    long releasedToday = 0;
    for (var cy : facCycles) {
      if ("in_progress".equals(cy.getStatus()) || "quarantined".equals(cy.getStatus()))
        activeCycle.putIfAbsent(cy.getSetId(), cy);
      if ("released".equals(cy.getStatus()) && cy.getReleasedAt()!=null
          && cy.getReleasedAt().atZoneSameInstant(ZoneId.of("Africa/Cairo")).toLocalDate().equals(today)) releasedToday++;
    }
    List<Map<String,Object>> rows = new ArrayList<>();
    long inProcess = 0, awaitingRelease = 0, quarantined = 0, recalled = 0, sterileAvailable = 0, expiringSoon = 0, expiredStock = 0;
    for (var s : sets.findByFacilityIdOrderByUpdatedAtDesc(fac)) {
      String expiryState = CssdBoardRules.expiryState(s.getStatus(), s.getExpiryDate(), today);
      Map<String,Object> r = new LinkedHashMap<>();
      r.put("set_id", s.getId()); r.put("set_code", s.getSetCode()); r.put("name", s.getName());
      r.put("status", s.getStatus()); r.put("contains_implants", s.isContainsImplants());
      r.put("storage_location", s.getStorageLocation()); r.put("expiry_date", s.getExpiryDate());
      r.put("expiry_state", expiryState);
      r.put("attention_rank", CssdBoardRules.setAttentionRank(s.getStatus(), expiryState));
      var cy = activeCycle.get(s.getId());
      if (cy != null) {
        var missing = CssdBoardRules.missingSteps(cy.isCleaningDone(), cy.isDisinfectionDone(), cy.isDried(), cy.isInspected(), cy.getPackedBy());
        boolean chain = CssdRules.chainComplete(cy.isCleaningDone(), cy.isDisinfectionDone(), cy.isDried(), cy.isInspected(), cy.getPackedBy());
        boolean releasable = CssdRules.releasable(chain, cy.getChemicalIndicator(), cy.getBiologicalIndicator(), s.isContainsImplants());
        r.put("cycle_id", cy.getId()); r.put("cycle_status", cy.getStatus());
        r.put("missing_steps", missing);
        r.put("chemical_indicator", cy.getChemicalIndicator()); r.put("biological_indicator", cy.getBiologicalIndicator());
        r.put("releasable", releasable);
        if ("in_process".equals(s.getStatus())) { inProcess++; if (releasable) awaitingRelease++; }
      }
      if ("quarantined".equals(s.getStatus())) quarantined++;
      if ("recalled".equals(s.getStatus())) recalled++;
      if ("sterile".equals(s.getStatus()) && "valid".equals(expiryState)) sterileAvailable++;
      if ("expiring_soon".equals(expiryState)) expiringSoon++;
      if ("expired".equals(expiryState)) expiredStock++;
      rows.add(r);
    }
    rows.sort(Comparator.comparingInt((Map<String,Object> r) -> (Integer) r.get("attention_rank")));
    List<Map<String,Object>> stRows = new ArrayList<>();
    long sterilizersAttention = 0;
    for (var st : sterilizers.findByFacilityIdOrderBySterilizerCodeAsc(fac)) {
      String mState = CssdBoardRules.dueState(st.getMaintenanceDue(), today);
      String cState = CssdBoardRules.dueState(st.getCalibrationDue(), today);
      boolean attention = "overdue".equals(mState) || "overdue".equals(cState);
      if (attention) sterilizersAttention++;
      Map<String,Object> r = new LinkedHashMap<>();
      r.put("sterilizer_code", st.getSterilizerCode()); r.put("status", st.getStatus());
      r.put("maintenance_due", st.getMaintenanceDue()); r.put("maintenance_state", mState);
      r.put("calibration_due", st.getCalibrationDue()); r.put("calibration_state", cState);
      r.put("attention", attention);
      stRows.add(r);
    }
    long issuedToday = usage.findByFacilityIdOrderByUsedAtDesc(fac).stream()
        .filter(u -> u.getUsedAt()!=null && u.getUsedAt().atZoneSameInstant(ZoneId.of("Africa/Cairo")).toLocalDate().equals(today)).count();
    Map<String,Object> summary = new LinkedHashMap<>();
    summary.put("in_process", inProcess); summary.put("awaiting_release", awaitingRelease);
    summary.put("quarantined", quarantined); summary.put("recalled", recalled);
    summary.put("sterile_available", sterileAvailable); summary.put("expiring_soon", expiringSoon);
    summary.put("expired_in_storage", expiredStock);
    summary.put("open_failures", failures.findByFacilityIdAndStatusOrderByCreatedAtDesc(fac, "open").size());
    summary.put("sterilizers_attention", sterilizersAttention);
    summary.put("released_today", releasedToday); summary.put("issued_today", issuedToday);
    Map<String,Object> out = new LinkedHashMap<>();
    out.put("ok", true); out.put("sets", rows); out.put("sterilizers", stRows); out.put("summary", summary);
    return out;
  }
  public record SterilizerRequest(String sterilizerCode, LocalDate maintenanceDue, LocalDate calibrationDue) {}
  @PostMapping("/sterilizers")
  Map<String,Object> sterilizer(@RequestBody SterilizerRequest r) {
    var p = principal();
    var s = sterilizers.save(new SterilizerRecord(UUID.fromString(p.facilityId()), p.governorate(), r.sterilizerCode(), r.maintenanceDue(), r.calibrationDue()));
    audit.record("cssd.sterilizer.registered","sterilizer_record",s.getId().toString(),"{}");
    return Map.of("ok",true,"sterilizer",s,"note","maintenance and calibration tracked per sterilizer");
  }
}
