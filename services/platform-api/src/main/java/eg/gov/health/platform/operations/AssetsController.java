package eg.gov.health.platform.operations;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/assets")
public class AssetsController {
  private final AssetRepository repo; private final AuditService audit;
  public AssetsController(AssetRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record AssetRequest(String assetCode, String name, LocalDate nextMaintenance) {}
  @PostMapping
  Map<String,Object> add(@RequestBody AssetRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.assetCode()==null||r.name()==null) throw BizException.badRequest("missing_field","assetCode و name مطلوبان");
    var a = repo.save(new Asset(UUID.fromString(p.facilityId()), p.governorate(), r.assetCode(), r.name(), r.nextMaintenance()));
    return Map.of("ok",true,"asset",a);
  }
  @PostMapping("/{id}/maintenance-complete")
  Map<String,Object> maintenance(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var a = repo.findById(id).orElseThrow(() -> BizException.notFound("asset_not_found","الأصل غير موجود"));
    LocalDate next = body.get("next_maintenance")==null ? LocalDate.now().plusMonths(6) : LocalDate.parse(body.get("next_maintenance"));
    a.completeMaintenance(next); repo.save(a);
    audit.record("asset.maintenance_completed","asset",id.toString(),"{}");
    return Map.of("ok",true,"asset",a);
  }
  @GetMapping("/due")
  Map<String,Object> due() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdAndNextMaintenanceLessThanEqual(UUID.fromString(p.facilityId()), LocalDate.now().plusDays(30));
    return Map.of("ok",true,"due_within_30_days",rows,"count",rows.size());
  }
  @GetMapping
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByNextMaintenanceAsc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"assets",rows,"count",rows.size());
  }
}
