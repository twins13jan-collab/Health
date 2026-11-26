package eg.gov.health.platform.privatesector;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.facility.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.time.temporal.ChronoUnit; import java.util.*;
/** Private-sector management console (board + licence upkeep).
 *  Every privately owned facility on the platform — private
 *  hospitals, clinics, laboratories, pharmacies, medical centres
 *  and the rest — is overseen here: grouped by management family,
 *  ranked by PrivateSectorRules so expired licences and pending
 *  approvals surface first. Approval and suspension themselves run
 *  through FacilityAdminController; this console adds the sector
 *  view and licence data upkeep. National admins see the whole
 *  sector; a governorate admin sees only their governorate. */
@RestController @RequestMapping("/api/admin/private-sector")
public class PrivateSectorController {
  private final FacilityRepository facilities; private final AuditService audit;
  private final eg.gov.health.platform.identity.AppUserRepository users;
  public PrivateSectorController(FacilityRepository facilities, AuditService audit,
                                 eg.gov.health.platform.identity.AppUserRepository users) {
    this.facilities = facilities; this.audit = audit; this.users = users;
  }
  private static final Set<String> FULL_TIERS = Set.of("super_admin", "admin", "national_admin");
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p == null || !(FULL_TIERS.contains(p.role())
        || Set.of("authority_admin", "governorate_admin", "directorate_admin").contains(p.role())))
      throw BizException.forbidden("إدارة القطاع الخاص لمسؤولي النظام والجهات الرقابية والمحافظات والمديريات");
    return p;
  }
  /** The caller's oversight scope over the facility register:
   *  system tiers oversee the whole private sector; an authority
   *  admin oversees its remit (MoH: ministry + private facilities,
   *  drug authority: pharmacies, insurance: insurance facilities,
   *  higher education: university facilities); governorate and
   *  directorate admins oversee the private sector in their area. */
  private boolean inScope(TenantContext.Principal p, Facility f) {
    if (FULL_TIERS.contains(p.role())) return "private".equals(f.getOwnership());
    return switch (p.role()) {
      case "authority_admin" -> users.findByEmail(p.email())
          .map(u -> eg.gov.health.platform.identity.AuthorityScope.covers(u.getAuthority(), f))
          .orElse(false);
      case "governorate_admin" -> "private".equals(f.getOwnership())
          && eg.gov.health.platform.facility.GovernorateNames.matches(p.governorate(), f.getGovernorate());
      case "directorate_admin" -> "private".equals(f.getOwnership())
          && p.directorate() != null && p.directorate().equals(f.getDirectorate());
      default -> false;
    };
  }

  @GetMapping("/board")
  Map<String,Object> board() {
    var p = principal();
    var today = LocalDate.now(ZoneId.of("Africa/Cairo"));
    var all = facilities.findAll().stream()
        .filter(f -> inScope(p, f))
        .sorted(Comparator.comparing(Facility::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
        .toList();
    List<Map<String,Object>> rows = new ArrayList<>();
    Map<String,Long> byGroup = new LinkedHashMap<>(Map.of("hospitals", 0L, "clinics", 0L, "labs", 0L, "pharmacies", 0L, "centers", 0L, "other", 0L));
    long pending = 0, active = 0, suspended = 0, expired = 0, expiring = 0, missing = 0;
    for (var f : all) {
      String group = PrivateSectorRules.typeGroup(f.getType());
      byGroup.merge(group, 1L, Long::sum);
      String lic = PrivateSectorRules.licenseState(f.getLicenseExpiry(), today);
      if ("pending".equals(f.getStatus())) pending++;
      if ("active".equals(f.getStatus())) active++;
      if ("suspended".equals(f.getStatus())) suspended++;
      if ("expired".equals(lic)) expired++;
      if ("expiring_soon".equals(lic)) expiring++;
      if ("not_recorded".equals(lic)) missing++;
      Map<String,Object> r = new LinkedHashMap<>();
      r.put("id", f.getId()); r.put("name_ar", f.getNameAr()); r.put("name_en", f.getNameEn());
      r.put("type", f.getType()); r.put("type_group", group); r.put("level", f.getLevel());
      r.put("governorate", f.getGovernorate()); r.put("directorate", f.getDirectorate()); r.put("city", f.getCity());
      r.put("status", f.getStatus()); r.put("bed_capacity", f.getBedCapacity());
      r.put("license_no", f.getLicenseNo()); r.put("license_authority", f.getLicenseAuthority());
      r.put("license_expiry", f.getLicenseExpiry()); r.put("license_state", lic);
      r.put("days_to_expiry", f.getLicenseExpiry() == null ? null : ChronoUnit.DAYS.between(today, f.getLicenseExpiry()));
      r.put("attention_rank", PrivateSectorRules.attentionRank(f.getStatus(), lic));
      r.put("created_at", f.getCreatedAt());
      rows.add(r);
    }
    rows.sort(Comparator.comparingInt((Map<String,Object> r) -> (Integer) r.get("attention_rank")));
    Map<String,Object> summary = new LinkedHashMap<>();
    summary.put("total", all.size()); summary.put("by_group", byGroup);
    summary.put("pending", pending); summary.put("active", active); summary.put("suspended", suspended);
    summary.put("license_expired", expired); summary.put("license_expiring_soon", expiring);
    summary.put("license_not_recorded", missing);
    Map<String,Object> out = new LinkedHashMap<>();
    out.put("ok", true); out.put("facilities", rows); out.put("count", rows.size()); out.put("summary", summary);
    return out;
  }

  public record LicenseBody(String licenseNo, String licenseAuthority, LocalDate licenseExpiry) {}
  @PostMapping("/{id}/license")
  Map<String,Object> updateLicense(@PathVariable UUID id, @RequestBody LicenseBody r) {
    var p = principal();
    var f = facilities.findById(id).orElseThrow(() -> BizException.notFound("facility_not_found","المنشأة غير موجودة"));
    if (!inScope(p, f))
      throw BizException.forbidden("هذه المنشأة خارج نطاق إشرافك");
    f.setLicense(r.licenseNo(), r.licenseAuthority(), r.licenseExpiry());
    facilities.save(f);
    audit.record("private_sector.license_updated","facility",id.toString(),"{}");
    return Map.of("ok", true, "facility", f,
        "license_state", PrivateSectorRules.licenseState(f.getLicenseExpiry(), LocalDate.now(ZoneId.of("Africa/Cairo"))));
  }
}
