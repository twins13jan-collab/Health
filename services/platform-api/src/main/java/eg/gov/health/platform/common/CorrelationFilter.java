package eg.gov.health.platform.common;
import jakarta.servlet.*; import jakarta.servlet.http.*;
import org.springframework.core.annotation.Order; import org.springframework.stereotype.Component;
import java.io.IOException; import java.util.UUID;
@Component @Order(1)
public class CorrelationFilter extends org.springframework.web.filter.OncePerRequestFilter {
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String cid = req.getHeader("X-Correlation-Id");
    if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();
    req.setAttribute("correlationId", cid);
    res.setHeader("X-Correlation-Id", cid);
    try { chain.doFilter(req, res); } finally { TenantContext.clear(); }
  }
}
