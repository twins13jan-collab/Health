package eg.gov.health.platform.softwarelifecycle;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Software lifecycle & releases (module 135). A register of
 *  applications, services, components and versions; development,
 *  test, staging and production environments kept separate;
 *  configuration managed without divergent copies; code review,
 *  build, signing, deployment and rollback governed; and
 *  third-party licences and components tracked. */
@RestController @RequestMapping("/api/software-lifecycle")
public class SoftwareLifecycleController {
  private final SoftwareComponentRepository components; private final SoftwareReleaseRepository releases; private final AuditService audit;
  public SoftwareLifecycleController(SoftwareComponentRepository components, SoftwareReleaseRepository releases, AuditService audit) {
    this.components=components; this.releases=releases; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record ComponentBody(String name, String kind, Boolean thirdParty, String licenceRef) {}
  @PostMapping("/components")
  Map<String,Object> create(@RequestBody ComponentBody r) {
    var p = principal();
    var c = components.save(new SoftwareComponent(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.kind(), Boolean.TRUE.equals(r.thirdParty()), r.licenceRef()));
    return Map.of("ok",true,"component",c);
  }
  public record ReleaseBody(String version, String environment, Boolean codeReviewed, Boolean buildSigned, Boolean configDrift) {}
  @PostMapping("/components/{id}/releases")
  Map<String,Object> release(@PathVariable UUID id, @RequestBody ReleaseBody r) {
    var p = principal();
    components.findById(id).orElseThrow(() -> BizException.notFound("component_not_found","المكون غير موجود"));
    var rel = releases.save(new SoftwareRelease(UUID.fromString(p.facilityId()), p.governorate(), id, r.version(), r.environment(),
      Boolean.TRUE.equals(r.codeReviewed()), Boolean.TRUE.equals(r.buildSigned()), Boolean.TRUE.equals(r.configDrift())));
    return Map.of("ok",true,"release",rel);
  }
  @PostMapping("/releases/{rid}/deploy")
  Map<String,Object> deploy(@PathVariable UUID rid) {
    principal();
    var rel = releases.findById(rid).orElseThrow(() -> BizException.notFound("release_not_found","الإصدار غير موجود"));
    var comp = components.findById(rel.getComponentId()).orElseThrow(() -> BizException.notFound("component_not_found","المكون غير موجود"));
    if (!deployable(rel, comp))
      throw BizException.conflict("not_deployable","إنتاج لا يستقبل إصدارًا غير مراجع أو غير موقع أو منجرف الإعدادات — ومكون طرف ثالث بلا ترخيص موثق لا يشحن");
    rel.deploy(); releases.save(rel);
    audit.record("lifecycle.release.deployed","software_release",rid.toString(),"{}");
    return Map.of("ok",true,"release",rel);
  }
  private boolean deployable(SoftwareRelease rel, SoftwareComponent comp) {
    return LifecycleRules.productionDeployable(rel.isCodeReviewed(), rel.isBuildSigned(), rel.isConfigDrift(), comp.isThirdParty(), comp.getLicenceRef());
  }
  public record RollbackBody(String reason) {}
  @PostMapping("/releases/{rid}/rollback")
  Map<String,Object> rollback(@PathVariable UUID rid, @RequestBody RollbackBody r) {
    principal();
    var rel = releases.findById(rid).orElseThrow(() -> BizException.notFound("release_not_found","الإصدار غير موجود"));
    if (!LifecycleRules.rollbackValid(r.reason()))
      throw BizException.badRequest("reason_required","تراجع عن إصدار فعل رسمي بسبب موثق");
    rel.rollback(r.reason()); releases.save(rel);
    audit.record("lifecycle.release.rolled_back","software_release",rid.toString(),"{}");
    return Map.of("ok",true,"release",rel);
  }
}
