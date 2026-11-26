package eg.gov.health.platform.settings;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class SettingsWebConfig implements WebMvcConfigurer {
  private final MaintenanceInterceptor interceptor;
  public SettingsWebConfig(MaintenanceInterceptor interceptor) { this.interceptor = interceptor; }
  @Override public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(interceptor); }
}
