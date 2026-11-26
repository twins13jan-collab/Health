package eg.gov.health.platform.identity;

import java.util.LinkedHashMap;
import java.util.Map;

/** The public view of an account — never includes the password hash. */
public final class UserViews {
  private UserViews() {}

  public static Map<String, Object> of(AppUser u) {
    Map<String, Object> v = new LinkedHashMap<>();
    v.put("id", u.getId());
    v.put("email", u.getEmail());
    v.put("full_name", u.getFullName() == null ? "" : u.getFullName());
    v.put("role", u.getRole());
    v.put("facility_id", u.getFacilityId() == null ? "" : u.getFacilityId());
    v.put("governorate", u.getGovernorate() == null ? "" : u.getGovernorate());
    v.put("directorate", u.getDirectorate() == null ? "" : u.getDirectorate());
    v.put("is_active", u.isActive());
    v.put("mfa_enabled", u.isMfaEnabled());
    v.put("last_login_at", u.getLastLoginAt() == null ? "" : u.getLastLoginAt().toString());
    v.put("created_at", u.getCreatedAt() == null ? "" : u.getCreatedAt().toString());
    return v;
  }
}
