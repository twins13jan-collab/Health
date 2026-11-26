package eg.gov.health.platform.identity;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.facility.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Approval queue for self-registered staff accounts. A person who
 *  registers under a facility (role → entity → governorate →
 *  directorate → facility) stays inactive until the facility's own
 *  manager approves the account — or a system tier above does.
 *  Managers see and decide only their own facility's queue. */
@RestController @RequestMapping("/api/facility-staff")
public class StaffApprovalController {
  private final AppUserRepository users; private final FacilityRepository facilities; private final AuditService audit;
  public StaffApprovalController(AppUserRepository users, FacilityRepository facilities, AuditService audit) {
    this.users = users; this.facilities = facilities; this.audit = audit;
  }
  private TenantContext.Principal guard() {
    var p = TenantContext.get();
    if (p == null || !Set.of("facility_manager", "super_admin", "admin", "national_admin").contains(p.role()))
      throw BizException.forbidden("اعتماد حسابات الطاقم لمدير المنشأة ومسؤولي النظام");
    return p;
  }
  private boolean facilityActive(UUID id) {
    return facilities.findById(id).map(f -> "active".equals(f.getStatus())).orElse(false);
  }
  private boolean inScope(TenantContext.Principal p, AppUser u) {
    if (!"facility_manager".equals(p.role())) return true;
    return p.facilityId() != null && UUID.fromString(p.facilityId()).equals(u.getFacilityId());
  }

  @GetMapping("/pending")
  Map<String,Object> pending() {
    var p = guard();
    List<AppUser> rows = "facility_manager".equals(p.role())
        ? (p.facilityId() == null ? List.of() : users.findByFacilityIdAndActiveFalse(UUID.fromString(p.facilityId())))
        : users.findByActiveFalse();
    var out = rows.stream()
        .filter(u -> u.getFacilityId() != null && !"patient".equals(u.getRole()) && facilityActive(u.getFacilityId()))
        .map(u -> {
          Map<String,Object> m = new LinkedHashMap<>();
          m.put("user", UserViews.of(u));
          m.put("facility_name", facilities.findById(u.getFacilityId()).map(Facility::getNameAr).orElse(null));
          return m;
        }).toList();
    return Map.of("pending", out, "count", out.size());
  }

  @PostMapping("/{id}/approve")
  Map<String,Object> approve(@PathVariable UUID id) {
    var p = guard();
    var u = users.findById(id).orElseThrow(() -> BizException.notFound("user_not_found", "الحساب غير موجود"));
    if (!inScope(p, u)) throw BizException.forbidden("هذا الحساب تابع لمنشأة أخرى");
    if (u.isActive()) throw BizException.conflict("already_active", "الحساب مفعّل بالفعل");
    u.setActive(true); users.save(u);
    audit.record("staff.approved", "app_user", id.toString(), "{}");
    return Map.of("ok", true, "user", UserViews.of(u));
  }

  @PostMapping("/{id}/reject")
  Map<String,Object> reject(@PathVariable UUID id) {
    var p = guard();
    var u = users.findById(id).orElseThrow(() -> BizException.notFound("user_not_found", "الحساب غير موجود"));
    if (!inScope(p, u)) throw BizException.forbidden("هذا الحساب تابع لمنشأة أخرى");
    if (u.isActive()) throw BizException.conflict("already_active", "لا يمكن رفض حساب مفعّل — عطّله من إدارة المستخدمين");
    users.delete(u);
    audit.record("staff.rejected", "app_user", id.toString(), "{}");
    return Map.of("ok", true);
  }
}
