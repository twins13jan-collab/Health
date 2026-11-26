package eg.gov.health.platform.config;

import eg.gov.health.platform.common.TenantContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

/**
 * Applies the RLS tenant setting (app.current_facility_id) to every pooled
 * connection at borrow time, from the JWT-derived TenantContext — the piece
 * that makes the database row-level-security policies (504 across the schema)
 * actually bind to application traffic. The setting is session-level
 * (is_local = false) because a pooled connection serves a whole request,
 * and it is reset to '' when the connection is returned so no tenant scope
 * can leak to the next borrower. Fail-closed: no principal means ''.
 *
 * On non-PostgreSQL data sources (unit tests) set_config does not exist;
 * failures there are ignored so the wrapper never breaks a test context.
 */
@Configuration
public class TenantDataSourceConfig implements BeanPostProcessor {

  private static final Logger log = LoggerFactory.getLogger(TenantDataSourceConfig.class);
  private static volatile boolean warnedOnce = false;

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof DataSource ds && !Proxy.isProxyClass(bean.getClass())) {
      return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {DataSource.class},
          new TenantDataSourceHandler(ds));
    }
    return bean;
  }

  private static void applySetting(Connection c, String value) {
    try (PreparedStatement ps = c.prepareStatement("SELECT set_config('app.current_facility_id', ?, false)")) {
      ps.setString(1, value == null ? "" : value);
      ps.execute();
    } catch (Exception e) {
      if (!warnedOnce) {
        warnedOnce = true;
        log.warn("Could not set app.current_facility_id (non-PostgreSQL datasource?): {}", e.getMessage());
      }
    }
  }

  private record TenantDataSourceHandler(DataSource delegate) implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Object result;
      try {
        result = method.invoke(delegate, args);
      } catch (InvocationTargetException e) {
        throw e.getCause();
      }
      if (result instanceof Connection conn && method.getName().equals("getConnection")) {
        applySetting(conn, TenantContext.rlsSetting());
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Connection.class},
            new TenantConnectionHandler(conn));
      }
      return result;
    }
  }

  private record TenantConnectionHandler(Connection delegate) implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getName().equals("close")) {
        applySetting(delegate, "");
      }
      try {
        return method.invoke(delegate, args);
      } catch (InvocationTargetException e) {
        throw e.getCause();
      }
    }
  }
}
