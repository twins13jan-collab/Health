package eg.gov.health.platform.accessmatrix;
import eg.gov.health.platform.common.TenantContext;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.*; import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;
/** Enforces the role matrix on writes. Rules load from the
 *  role_permissions table and refresh every 60 seconds, so a
 *  matrix change takes effect without a redeploy. */
@Component
public class AccessMatrixInterceptor implements HandlerInterceptor {
  private final RolePermissionRepository repo;
  private final AtomicReference<List<AccessMatrixRules.Rule>> cache = new AtomicReference<>(List.of());
  private final AtomicLong loadedAt = new AtomicLong(0);
  public AccessMatrixInterceptor(RolePermissionRepository repo) { this.repo = repo; }
  private List<AccessMatrixRules.Rule> rules() {
    long now = System.currentTimeMillis();
    if (now - loadedAt.get() > 60_000 || cache.get().isEmpty()) {
      try {
        var loaded = repo.findAll().stream()
          .map(r -> new AccessMatrixRules.Rule(r.getPathPrefix(),
            Arrays.stream(r.getAllowedRoles().split(",")).map(String::trim).collect(Collectors.toSet())))
          .toList();
        cache.set(loaded); loadedAt.set(now);
      } catch (Exception ignored) { /* keep last known rules; tenancy checks still apply */ }
    }
    return cache.get();
  }
  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
    String path = req.getRequestURI();
    if (!path.startsWith("/api/") || path.startsWith("/api/auth") || path.startsWith("/api/public") || path.startsWith("/api/admin")) return true;
    var p = TenantContext.get();
    if (p == null) return true; // authentication layer decides unauthenticated requests
    boolean write = Set.of("POST","PUT","PATCH","DELETE").contains(req.getMethod());
    boolean ok = write
        ? AccessMatrixRules.writeAllowed(rules(), path, p.role())
        : AccessMatrixRules.readAllowed(rules(), path, p.role());
    if (ok) return true;
    res.setStatus(403);
    res.setContentType("application/json;charset=UTF-8");
    res.getWriter().write("{\"error\":\"role_forbidden\",\"message\":\"الدور " + p.role()
        + (write ? " غير مصرح له بالكتابة" : " غير مصرح له بالاطلاع")
        + " في هذا النطاق — راجع مصفوفة الصلاحيات\"}");
    return false;
  }
}
