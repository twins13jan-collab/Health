package eg.gov.health.platform.identity;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.facility.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** Facility administration for the central admin panel: every
 *  facility in every state — including self-registered ones
 *  waiting as 'pending' — can be reviewed, approved, suspended,
 *  and edited here. Approving a facility also activates the
 *  manager account created with its registration. System
 *  administrators only; every action is audited. */
@RestController @RequestMapping("/api/admin/facilities")
public class FacilityAdminController {
  private final FacilityRepository facilities;
  private final AppUserRepository users;
  private final AuditService audit;

  public FacilityAdminController(FacilityRepository facilities, AppUserRepository users, AuditService audit) {
    this.facilities = facilities; this.users = users; this.audit = audit;
  }

  private void requireAdmin() {
    var p = TenantContext.get();
    if (p == null || !(Set.of("super_admin", "admin", "national_admin").contains(p.role())))
      throw BizException.forbidden("إدارة المنشآت لمسؤولي النظام فقط");
  }

  @GetMapping
  Map<String, Object> list(@RequestParam(required = false) String status) {
    requireAdmin();
    var rows = facilities.findAll().stream()
        .filter(f -> status == null || status.equals(f.getStatus()))
        .sorted(Comparator.comparing(Facility::getCreatedAt).reversed())
        .toList();
    return Map.of("facilities", rows, "count", rows.size(),
        "pending", facilities.findAll().stream().filter(f -> "pending".equals(f.getStatus())).count());
  }

  @PostMapping("/{id}/approve")
  Map<String, Object> approve(@PathVariable UUID id) {
    requireAdmin();
    var f = facilities.findById(id).orElseThrow(() -> BizException.notFound("facility_not_found", "المنشأة غير موجودة"));
    f.setStatus("active");
    facilities.save(f);
    int activated = 0;
    for (var u : users.findAll()) {
      if (id.equals(u.getFacilityId()) && !u.isActive()) { u.setActive(true); users.save(u); activated++; }
    }
    audit.record("admin.facility_approved", "facility", String.valueOf(id),
        "{\"activatedUsers\":" + activated + "}");
    return Map.of("facility", f, "activatedUsers", activated);
  }

  public record StatusBody(String status) {}
  @PostMapping("/{id}/status")
  Map<String, Object> setStatus(@PathVariable UUID id, @RequestBody StatusBody r) {
    requireAdmin();
    if (!Set.of("active", "pending", "suspended").contains(r.status()))
      throw BizException.badRequest("status_invalid", "حالة غير معروفة");
    var f = facilities.findById(id).orElseThrow(() -> BizException.notFound("facility_not_found", "المنشأة غير موجودة"));
    f.setStatus(r.status());
    facilities.save(f);
    audit.record("admin.facility_status", "facility", String.valueOf(id), "{\"status\":\"" + r.status() + "\"}");
    return Map.of("facility", f);
  }
}
