package eg.gov.health.platform.servicedesk;
import java.time.*; import java.util.Map;
/** Service-centre rules (module 139). Every report is tied to its
 *  service, facility and device, classified and answered against a
 *  severity SLA. Two kinds of reports NEVER travel the standard
 *  desk path: security incidents go to the security track and
 *  clinical-safety reports to the clinical track — each resolved
 *  only inside its own track by its own authority. Resolution time
 *  and satisfaction are measured, and proven fixes feed the
 *  knowledge base. */
public final class ServiceDeskRules {
  private ServiceDeskRules() {}
  public static final Map<String,Integer> RESPONSE_TARGET_MIN = Map.of("critical", 15, "high", 60, "medium", 240, "low", 480);
  public static String routeTrack(String category) {
    if ("security".equals(category)) return "security";
    if ("clinical_safety".equals(category)) return "clinical";
    return "standard";
  }
  public static boolean mayResolve(String track, String role) {
    return switch (track == null ? "standard" : track) {
      case "security" -> "admin".equals(role) || "security_officer".equals(role);
      case "clinical" -> "admin".equals(role) || "clinical_lead".equals(role);
      default -> role != null;
    };
  }
  public static boolean responseBreached(String severity, OffsetDateTime createdAt, OffsetDateTime firstResponseAt) {
    if (createdAt == null || firstResponseAt == null) return false;
    long mins = Duration.between(createdAt, firstResponseAt).toMinutes();
    return mins > RESPONSE_TARGET_MIN.getOrDefault(severity == null ? "low" : severity, 480);
  }
}
