package eg.gov.health.platform.settings;
import eg.gov.health.platform.common.TenantContext;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Set;
/** Maintenance mode: while system.maintenance_mode is on, write
 *  operations across the platform are paused (503) for everyone
 *  except system administrators, who keep full control so they can
 *  finish the maintenance and switch it off. Authentication and the
 *  admin surface itself always stay reachable, and reads are never
 *  blocked. */
@Component
public class MaintenanceInterceptor implements HandlerInterceptor {
  private final SettingsService settings;
  public MaintenanceInterceptor(SettingsService settings) { this.settings = settings; }
  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
    if (!Set.of("POST", "PUT", "PATCH", "DELETE").contains(req.getMethod())) return true;
    String path = req.getRequestURI();
    if (!path.startsWith("/api/") || path.startsWith("/api/auth") || path.startsWith("/api/admin")) return true;
    if (!settings.getBool("system.maintenance_mode", false)) return true;
    var p = TenantContext.get();
    if (p != null && Set.of("admin", "national_admin").contains(p.role())) return true;
    res.setStatus(503);
    res.setContentType("application/json;charset=UTF-8");
    String msg = settings.get("system.maintenance_message", "المنظومة في صيانة مجدولة");
    res.getWriter().write("{\"error\":\"maintenance_mode\",\"message\":\"" + msg.replace("\"", "'") + "\"}");
    return false;
  }
}
