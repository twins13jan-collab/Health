package eg.gov.health.platform.config;
import eg.gov.health.platform.common.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*; import jakarta.servlet.http.*;
import org.springframework.context.annotation.*; import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException; import java.util.List;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
  private final JwtService jwt;
  public SecurityConfig(JwtService jwt) { this.jwt = jwt; }
  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
  @Bean SecurityFilterChain chain(HttpSecurity http) throws Exception {
    http.csrf(c -> c.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a -> a
        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        .requestMatchers("/api/public/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/register-patient", "/api/auth/register-facility", "/api/auth/register-staff").permitAll()
        .requestMatchers("/fhir/**").authenticated()
        .anyRequest().authenticated())
      .addFilterBefore(new JwtFilter(jwt), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
  static class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwt; JwtFilter(JwtService jwt) { this.jwt = jwt; }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
        throws ServletException, IOException {
      String h = req.getHeader("Authorization");
      if ((h == null || !h.startsWith("Bearer ")) && req.getRequestURI().startsWith("/api/public/")) {
        // Anonymous public aggregates read under the dedicated 'public'
        // tenant setting (SELECT-only RLS policies, counts only).
        TenantContext.set(new TenantContext.Principal("public", null, "public", null, null, null));
      }
      if (h != null && h.startsWith("Bearer ")) {
        try {
          Claims c = jwt.parse(h.substring(7));
          TenantContext.set(new TenantContext.Principal(c.getSubject(), c.get("email", String.class),
            c.get("role", String.class), c.get("facility_id", String.class),
            c.get("governorate", String.class), c.get("directorate", String.class)));
          var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            c.getSubject(), null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + c.get("role", String.class))));
          org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) { /* unauthenticated: security chain will 401 protected routes */ }
      }
      chain.doFilter(req, res);
    }
  }
}
