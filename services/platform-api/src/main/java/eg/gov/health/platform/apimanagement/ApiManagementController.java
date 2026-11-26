package eg.gov.health.platform.apimanagement;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** APIs & data products (module 125). A central register of
 *  APIs, versions and owners; authentication, authorization,
 *  quotas and key revocation managed; message schemas and
 *  backward compatibility governed; a developer portal and test
 *  environments; and usage, errors and cost measured. */
@RestController @RequestMapping("/api/api-management")
public class ApiManagementController {
  private final ApiProductRepository products; private final ApiVersionRepository versions;
  private final ApiConsumerRepository consumers; private final AuditService audit;
  public ApiManagementController(ApiProductRepository products, ApiVersionRepository versions, ApiConsumerRepository consumers, AuditService audit) {
    this.products=products; this.versions=versions; this.consumers=consumers; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ApiProduct product(UUID id) {
    return products.findById(id).orElseThrow(() -> BizException.notFound("product_not_found","المنتج غير موجود"));
  }

  public record ProductBody(String name, String ownerRef, String currentVersion) {}
  @PostMapping("/products")
  Map<String,Object> create(@RequestBody ProductBody r) {
    var p = principal();
    var pr = products.save(new ApiProduct(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.ownerRef(), r.currentVersion()));
    return Map.of("ok",true,"product",pr);
  }
  public record VersionBody(String version, Boolean backwardCompatible, String breakingChangeNote) {}
  @PostMapping("/products/{id}/versions")
  Map<String,Object> addVersion(@PathVariable UUID id, @RequestBody VersionBody r) {
    var p = principal(); product(id);
    var v = versions.save(new ApiVersion(UUID.fromString(p.facilityId()), p.governorate(), id, r.version(), Boolean.TRUE.equals(r.backwardCompatible()), r.breakingChangeNote()));
    return Map.of("ok",true,"api_version",v);
  }
  @PostMapping("/versions/{vid}/deprecate")
  Map<String,Object> deprecate(@PathVariable UUID vid) {
    principal();
    var v = versions.findById(vid).orElseThrow(() -> BizException.notFound("version_not_found","الإصدار غير موجود"));
    v.deprecate(); versions.save(v);
    return Map.of("ok",true,"api_version",v,"note","deprecation window open — consumers migrate, nothing breaks silently");
  }
  @PostMapping("/versions/{vid}/make-current")
  Map<String,Object> makeCurrent(@PathVariable UUID vid) {
    principal();
    var v = versions.findById(vid).orElseThrow(() -> BizException.notFound("version_not_found","الإصدار غير موجود"));
    var pr = productOf(v);
    boolean previousDeprecated = versions.findByProductId(pr.getId()).stream()
      .filter(x -> x.getVersion().equals(pr.getCurrentVersion())).allMatch(ApiVersion::isDeprecated);
    if (!ApiRules.versionPublishable(v.isBackwardCompatible(), previousDeprecated))
      throw BizException.conflict("breaking_without_window","إصدار كاسر للتوافق لا يحل محل الحي قبل فتح نافذة إهمال لسابقه");
    pr.setCurrentVersion(v.getVersion()); products.save(pr);
    audit.record("api.version.current","api_version",vid.toString(),"{}");
    return Map.of("ok",true,"product",pr,"current_version",v.getVersion());
  }
  private ApiProduct productOf(ApiVersion v) {
    return product(v.getProductId());
  }
  public record ConsumerBody(String consumerRef, Integer quotaPerDay) {}
  @PostMapping("/products/{id}/consumers")
  Map<String,Object> consumer(@PathVariable UUID id, @RequestBody ConsumerBody r) {
    var p = principal(); product(id);
    var c = consumers.save(new ApiConsumer(UUID.fromString(p.facilityId()), p.governorate(), id, r.consumerRef(), r.quotaPerDay()));
    return Map.of("ok",true,"consumer",c);
  }
  @PostMapping("/consumers/{cid}/revoke-key")
  Map<String,Object> revoke(@PathVariable UUID cid) {
    principal();
    var c = consumers.findById(cid).orElseThrow(() -> BizException.notFound("consumer_not_found","المستهلك غير موجود"));
    c.revokeKey(); consumers.save(c);
    audit.record("api.key.revoked","api_consumer",cid.toString(),"{}");
    return Map.of("ok",true,"consumer",c);
  }
  @GetMapping("/consumers/{cid}/access")
  Map<String,Object> access(@PathVariable UUID cid) {
    principal();
    var c = consumers.findById(cid).orElseThrow(() -> BizException.notFound("consumer_not_found","المستهلك غير موجود"));
    boolean allowed = ApiRules.consumerAllowed(c.getKeyStatus(), c.getUsageToday(), c.getQuotaPerDay());
    return Map.of("ok",true,"consumer_id",cid.toString(),"key_status",c.getKeyStatus(),"usage_today",c.getUsageToday(),"allowed",allowed);
  }
}
