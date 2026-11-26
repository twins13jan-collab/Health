package eg.gov.health.platform.identity;
import eg.gov.health.platform.common.*; import eg.gov.health.platform.config.JwtService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.*;

/** Identity endpoints: hardened login (lockout after repeated failures,
 *  audit on every outcome), the current-session profile, and self-service
 *  password change. Account administration lives in UserAdminController. */
@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final AppUserRepository users; private final PasswordEncoder encoder; private final JwtService jwt; private final AuditService audit;
  private final eg.gov.health.platform.facility.FacilityRepository facilities;
  private final eg.gov.health.platform.settings.SettingsService settings;
  public AuthController(AppUserRepository users, PasswordEncoder encoder, JwtService jwt, AuditService audit,
                        eg.gov.health.platform.facility.FacilityRepository facilities,
                        eg.gov.health.platform.settings.SettingsService settings) {
    this.users = users; this.encoder = encoder; this.jwt = jwt; this.audit = audit; this.facilities = facilities;
    this.settings = settings;
  }

  public record LoginRequest(@NotBlank String email, @NotBlank String password, String facilityId) {}

  /** Hierarchical sign-in: when the user picks the facility they
   *  are signing in under (entity → governorate → directorate →
   *  facility), the account must actually belong to that scope —
   *  a facility account to that facility, an oversight account to
   *  a facility inside its remit. A mismatch is refused plainly. */
  private void checkLoginScope(AppUser user, String facilityId) {
    if (facilityId == null || facilityId.isBlank()) return;
    UUID fid;
    try { fid = UUID.fromString(facilityId.trim()); }
    catch (IllegalArgumentException e) { throw BizException.badRequest("facility_invalid", "معرّف المنشأة غير صالح"); }
    var facility = facilities.findById(fid)
        .orElseThrow(() -> BizException.badRequest("facility_not_found", "المنشأة المختارة غير موجودة"));
    boolean ok;
    if (user.getFacilityId() != null) {
      ok = fid.equals(user.getFacilityId());
    } else {
      ok = switch (user.getRole()) {
        case "governorate_admin" -> eg.gov.health.platform.facility.GovernorateNames.matches(user.getGovernorate(), facility.getGovernorate());
        case "directorate_admin" -> facility.getDirectorate() != null && facility.getDirectorate().equals(user.getDirectorate());
        case "authority_admin" -> AuthorityScope.covers(user.getAuthority(), facility);
        default -> true;
      };
    }
    if (!ok) {
      audit.record("auth.login_scope_denied", "app_user", user.getId().toString(), "{}");
      throw BizException.forbidden("هذا الحساب لا يتبع المنشأة المختارة «" + facility.getNameAr() + "» — راجع الجهة والمحافظة والمديرية والمنشأة");
    }
  }
  public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank String newPassword) {}

  @PostMapping("/login")
  Map<String, Object> login(@RequestBody @jakarta.validation.Valid LoginRequest r) {
    OffsetDateTime now = OffsetDateTime.now();
    var user = users.findByEmail(r.email())
      .orElseThrow(() -> BizException.badRequest("invalid_credentials", "بيانات الدخول غير صحيحة"));
    int maxFailures = settings.getInt("security.lockout_max_failures", LockoutPolicy.MAX_FAILURES);
    long lockMinutes = settings.getInt("security.lockout_minutes", (int) LockoutPolicy.LOCK_MINUTES);
    if (LockoutPolicy.isLocked(user.getLockedUntil(), now)) {
      audit.record("auth.login_locked", "app_user", user.getId().toString(), "{}");
      throw BizException.forbidden("الحساب مقفل مؤقتًا بسبب محاولات دخول فاشلة متكررة — حاول بعد " + lockMinutes + " دقيقة");
    }
    if (!user.isActive() || !encoder.matches(r.password(), user.getPasswordHash())) {
      int failures = LockoutPolicy.nextFailureCount(user.getFailedLoginAttempts());
      if (LockoutPolicy.shouldLock(failures, maxFailures)) {
        user.setLockedUntil(LockoutPolicy.lockUntil(now, lockMinutes));
        user.setFailedLoginAttempts(0);
        audit.record("auth.account_locked", "app_user", user.getId().toString(), "{}");
      } else {
        user.setFailedLoginAttempts(failures);
      }
      users.save(user);
      audit.record("auth.login_failed", "app_user", user.getId().toString(), "{}");
      throw BizException.badRequest("invalid_credentials", "بيانات الدخول غير صحيحة");
    }
    checkLoginScope(user, r.facilityId());
    user.setFailedLoginAttempts(0);
    user.setLockedUntil(null);
    user.setLastLoginAt(now);
    users.save(user);
    audit.record("auth.login", "app_user", user.getId().toString(), "{}");
    String token = jwt.issue(user.getId().toString(), user.getEmail(), user.getRole(),
      user.getFacilityId() == null ? null : user.getFacilityId().toString(), user.getGovernorate(), user.getDirectorate());
    return Map.of("ok", true, "access_token", token, "token_type", "Bearer", "user", UserViews.of(user));
  }

  @GetMapping("/me")
  Map<String, Object> me() {
    var p = TenantContext.get();
    if (p == null) throw BizException.forbidden("لا توجد جلسة نشطة");
    var user = users.findById(UUID.fromString(p.userId()))
      .orElseThrow(() -> BizException.notFound("user_not_found", "المستخدم غير موجود"));
    return UserViews.of(user);
  }

  @PostMapping("/change-password")
  Map<String, Object> changePassword(@RequestBody @jakarta.validation.Valid ChangePasswordRequest r) {
    var p = TenantContext.get();
    if (p == null) throw BizException.forbidden("لا توجد جلسة نشطة");
    var user = users.findById(UUID.fromString(p.userId()))
      .orElseThrow(() -> BizException.notFound("user_not_found", "المستخدم غير موجود"));
    if (!encoder.matches(r.currentPassword(), user.getPasswordHash()))
      throw BizException.badRequest("invalid_credentials", "كلمة المرور الحالية غير صحيحة");
    var violations = PasswordPolicy.violations(r.newPassword(),
        settings.getInt("security.password_min_length", PasswordPolicy.MIN_LENGTH));
    if (!violations.isEmpty())
      throw BizException.badRequest("weak_password", String.join("؛ ", violations));
    user.setPasswordHash(encoder.encode(r.newPassword()));
    users.save(user);
    audit.record("auth.password_changed", "app_user", user.getId().toString(), "{}");
    return Map.of("ok", true);
  }
}
