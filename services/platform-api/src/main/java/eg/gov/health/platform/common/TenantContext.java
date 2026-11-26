package eg.gov.health.platform.common;
/** Facility (tenant) scope for the current request, taken ONLY from the JWT —
 *  never from client-supplied ids (Catalog v6 rule). Consumed by the RLS
 *  DataSource wrapper to set app.current_facility_id per connection. */
public final class TenantContext {
  public record Principal(String userId, String email, String role, String facilityId, String governorate, String directorate) {}
  private static final ThreadLocal<Principal> CURRENT = new ThreadLocal<>();
  private TenantContext() {}
  public static void set(Principal p) { CURRENT.set(p); }
  public static Principal get() { return CURRENT.get(); }
  public static void clear() { CURRENT.remove(); }
  /** RLS setting value: facility uuid, 'gov:<governorate>', or 'national'. */
  public static String rlsSetting() {
    Principal p = CURRENT.get();
    if (p == null) return "";
    if ("national_admin".equals(p.role())) return "national";
    if ("public".equals(p.role())) return "public";
    if ("patient".equals(p.role()) && p.userId() != null) return "patient:" + p.userId();
    if (p.facilityId() != null && !p.facilityId().isBlank()) return p.facilityId();
    if (("governorate_admin".equals(p.role()) || "directorate_admin".equals(p.role())) && p.governorate() != null)
      return "gov:" + p.governorate();
    return "";
  }
}
