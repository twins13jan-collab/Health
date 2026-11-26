package eg.gov.health.platform.accessmatrix;
import java.util.*;
/** Role-matrix matching. The longest matching path prefix governs
 *  a domain. Since the full-matrix batch (V103) every domain of the
 *  platform carries a rule, and the defaults are closed: a WRITE to
 *  a path with no governing rule is refused for everyone except the
 *  system-administration tiers, and a READ of a governed domain is
 *  limited to its role set (the oversight tiers — governorate and
 *  directorate admins — may read across domains but write only
 *  where a rule names them). `allowed` keeps the original
 *  rule-check semantics for compatibility. */
public final class AccessMatrixRules {
  private AccessMatrixRules() {}
  public record Rule(String pathPrefix, Set<String> allowedRoles) {}
  public static Rule governing(List<Rule> rules, String path) {
    Rule best = null;
    for (Rule r : rules) {
      if (path.equals(r.pathPrefix()) || path.startsWith(r.pathPrefix() + "/")) {
        if (best == null || r.pathPrefix().length() > best.pathPrefix().length()) best = r;
      }
    }
    return best;
  }
  public static boolean allowed(List<Rule> rules, String path, String role) {
    if (role == null) return false;
    if ("admin".equals(role)) return true;
    Rule g = governing(rules, path);
    return g == null || g.allowedRoles().contains(role);
  }
  private static final Set<String> ADMIN_TIERS = Set.of("super_admin", "admin");
  private static final Set<String> OVERSIGHT_TIERS = Set.of("super_admin", "admin", "national_admin", "governorate_admin", "directorate_admin");
  /** Closed default for writes: ungoverned domains refuse everyone
   *  but the administration tiers. */
  public static boolean writeAllowed(List<Rule> rules, String path, String role) {
    if (role == null) return false;
    if (ADMIN_TIERS.contains(role)) return true;
    Rule g = governing(rules, path);
    return g != null && g.allowedRoles().contains(role);
  }
  /** Reads: ungoverned paths stay readable; governed domains are
   *  limited to their role set plus the oversight tiers. */
  public static boolean readAllowed(List<Rule> rules, String path, String role) {
    if (role == null) return false;
    if (OVERSIGHT_TIERS.contains(role)) return true;
    Rule g = governing(rules, path);
    return g == null || g.allowedRoles().contains(role);
  }
}
