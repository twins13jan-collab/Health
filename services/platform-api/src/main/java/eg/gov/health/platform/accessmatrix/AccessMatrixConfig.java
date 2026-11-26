package eg.gov.health.platform.accessmatrix;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class AccessMatrixConfig implements WebMvcConfigurer {
  private final AccessMatrixInterceptor interceptor;
  public AccessMatrixConfig(AccessMatrixInterceptor interceptor) { this.interceptor = interceptor; }
  @Override public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(interceptor); }
}
