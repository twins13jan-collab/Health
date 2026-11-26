package eg.gov.health.platform.toxicology;
import java.time.*; import java.util.Set;
/** Poison-centre rules (cycle 254). The registry supports analysis and
 *  approved consultation — it never generates unapproved treatment
 *  recommendations: every recommendation binds to a named toxicology
 *  consultant, a documented reference and a contact time; intake staff
 *  record information but never advise; uncertainty about substance,
 *  amount and timing is shown, never hidden; references must be current —
 *  stale or inactive references cannot back a recommendation; when the
 *  centre is unavailable the unavailability itself is recorded with the
 *  alternative path, and no recommendation is issued. */
public final class ToxicologyRules {
  private ToxicologyRules() {}
  /** Roles allowed to issue a toxicology consultation. */
  public static final Set<String> CONSULTANT_ROLES = Set.of("toxicologist","physician","poison_center_specialist","admin");
  public static boolean mayConsult(String role) { return role != null && CONSULTANT_ROLES.contains(role); }
  /** A reference backs recommendations only while active and reviewed
   *  within the last 24 months. */
  public static boolean referenceCurrent(boolean active, LocalDate reviewDate, LocalDate today) {
    return active && reviewDate != null && today != null && !reviewDate.isBefore(today.minusMonths(24));
  }
  /** Unknown substance is honest only with a container/source description —
   *  never an assumption. */
  public static boolean unknownSubstanceDocumented(boolean unknown, String containerDescription) {
    return !unknown || (containerDescription != null && !containerDescription.isBlank());
  }
  /** No recommendation may be attached to an unavailable-centre record. */
  public static boolean recommendationAllowed(boolean centerUnavailable, String recommendation) {
    return !centerUnavailable || recommendation == null || recommendation.isBlank();
  }
}
