package eg.gov.health.platform.identity;
import eg.gov.health.platform.common.*;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** Account administration — production replacement for seed-only accounts.
 *  Restricted to system administrators (role admin / national_admin); the
 *  role-matrix interceptor deliberately does not govern identity paths, so
 *  the check is enforced here on every method. Every action is audited. */
@RestController @RequestMapping("/api/admin/users")
public class UserAdminController {
  private final AppUserRepository users; private final PasswordEncoder encoder; private final AuditService audit;
  private final eg.gov.health.platform.settings.SettingsService settings;
  public UserAdminController(AppUserRepository users, PasswordEncoder encoder, AuditService audit,
                             eg.gov.health.platform.settings.SettingsService settings) {
    this.users = users; this.encoder = encoder; this.audit = audit; this.settings = settings;
  }
  private java.util.List<String> passwordViolations(String password) {
    return PasswordPolicy.violations(password,
        settings.getInt("security.password_min_length", PasswordPolicy.MIN_LENGTH));
  }

  public record CreateUserRequest(@NotBlank @Email String email, @NotBlank String password,
      @NotBlank String role, String facilityId, String governorate, String directorate, String fullName) {}
  public record UpdateUserRequest(String role, String facilityId, String governorate, String directorate, String fullName) {}
  public record ResetPasswordRequest(@NotBlank String newPassword) {}

  private TenantContext.Principal requireAdmin() {
    var p = TenantContext.get();
    if (p == null || !("super_admin".equals(p.role()) || "admin".equals(p.role())))
      throw BizException.forbidden("إدارة الحسابات مقصورة على مسؤولي النظام");
    return p;
  }
  private void requireSuperFor(String targetRole, TenantContext.Principal p) {
    if ("super_admin".equals(targetRole) && !"super_admin".equals(p.role()))
      throw BizException.forbidden("إنشاء حساب أدمن موسّع أو تعديله للأدمن الموسّع فقط");
  }

  private AppUser load(UUID id) {
    return users.findById(id).orElseThrow(() -> BizException.notFound("user_not_found", "المستخدم غير موجود"));
  }

  private static UUID facilityOrNull(String facilityId) {
    return facilityId == null || facilityId.isBlank() ? null : UUID.fromString(facilityId);
  }

  @GetMapping
  Map<String, Object> list(@RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    requireAdmin();
    var pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by("createdAt").descending());
    Page<AppUser> result = (q == null || q.isBlank())
      ? users.findAll(pageable)
      : users.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(q, q, pageable);
    return Map.of("items", result.getContent().stream().map(UserViews::of).toList(),
      "total", result.getTotalElements(), "page", result.getNumber(), "size", result.getSize());
  }

  @PostMapping
  Map<String, Object> create(@RequestBody @jakarta.validation.Valid CreateUserRequest r) {
    var caller = requireAdmin();
    requireSuperFor(r.role(), caller);
    if (users.existsByEmail(r.email()))
      throw BizException.conflict("email_exists", "يوجد حساب بهذا البريد الإلكتروني بالفعل");
    var violations = passwordViolations(r.password());
    if (!violations.isEmpty())
      throw BizException.badRequest("weak_password", String.join("؛ ", violations));
    var user = new AppUser(r.email(), encoder.encode(r.password()), r.role(),
      facilityOrNull(r.facilityId()), r.governorate(), r.directorate(), r.fullName());
    users.save(user);
    audit.record("admin.user_created", "app_user", user.getId().toString(),
      "{\"role\":\"" + r.role() + "\"}");
    return UserViews.of(user);
  }

  @PutMapping("/{id}")
  Map<String, Object> update(@PathVariable UUID id, @RequestBody UpdateUserRequest r) {
    var caller = requireAdmin();
    requireSuperFor(r.role(), caller);
    var user = load(id);
    if ("super_admin".equals(user.getRole())) requireSuperFor("super_admin", caller);
    if (r.role() != null && !r.role().isBlank()) user.setRole(r.role());
    if (r.facilityId() != null) user.setFacilityId(facilityOrNull(r.facilityId()));
    if (r.governorate() != null) user.setGovernorate(r.governorate());
    if (r.directorate() != null) user.setDirectorate(r.directorate());
    if (r.fullName() != null) user.setFullName(r.fullName());
    users.save(user);
    audit.record("admin.user_updated", "app_user", user.getId().toString(), "{}");
    return UserViews.of(user);
  }

  @PostMapping("/{id}/activate")
  Map<String, Object> activate(@PathVariable UUID id) {
    requireAdmin();
    var user = load(id);
    user.setActive(true);
    users.save(user);
    audit.record("admin.user_activated", "app_user", user.getId().toString(), "{}");
    return UserViews.of(user);
  }

  @PostMapping("/{id}/deactivate")
  Map<String, Object> deactivate(@PathVariable UUID id) {
    var p = requireAdmin();
    if (p.userId().equals(id.toString()))
      throw BizException.conflict("cannot_deactivate_self", "لا يمكنك تعطيل حسابك الحالي");
    var user = load(id);
    user.setActive(false);
    users.save(user);
    audit.record("admin.user_deactivated", "app_user", user.getId().toString(), "{}");
    return UserViews.of(user);
  }

  @PostMapping("/{id}/reset-password")
  Map<String, Object> resetPassword(@PathVariable UUID id, @RequestBody @jakarta.validation.Valid ResetPasswordRequest r) {
    requireAdmin();
    var violations = passwordViolations(r.newPassword());
    if (!violations.isEmpty())
      throw BizException.badRequest("weak_password", String.join("؛ ", violations));
    var user = load(id);
    user.setPasswordHash(encoder.encode(r.newPassword()));
    user.setFailedLoginAttempts(0);
    user.setLockedUntil(null);
    users.save(user);
    audit.record("admin.user_password_reset", "app_user", user.getId().toString(), "{}");
    return Map.of("ok", true);
  }
}
