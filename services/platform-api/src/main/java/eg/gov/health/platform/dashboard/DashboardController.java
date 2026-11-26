package eg.gov.health.platform.dashboard;
import eg.gov.health.platform.common.TenantContext;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** The operational dashboard API the web shell was built against:
 *  national (or caller-scoped) KPIs and capacity by governorate,
 *  computed live from the operational tables. */
@RestController @RequestMapping("/api/dashboard")
public class DashboardController {
  private final Aggregates aggregates;
  public DashboardController(Aggregates aggregates) { this.aggregates = aggregates; }
  static Aggregates.Scope scopeOf() {
    var p = TenantContext.get();
    if (p == null) return Aggregates.Scope.national();
    if (p.facilityId() != null && !p.facilityId().isBlank())
      return new Aggregates.Scope(UUID.fromString(p.facilityId()), null);
    if (("governorate_admin".equals(p.role()) || "directorate_admin".equals(p.role())) && p.governorate() != null)
      return new Aggregates.Scope(null, p.governorate());
    return Aggregates.Scope.national();
  }
  @GetMapping("/kpis")
  Map<String, Object> kpis() { return aggregates.kpis(scopeOf()); }
  @GetMapping("/capacity-by-governorate")
  List<Map<String, Object>> capacity() { return aggregates.capacityByGovernorate(scopeOf()); }
}
