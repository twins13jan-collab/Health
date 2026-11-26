package eg.gov.health.platform.modulespec;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Module specification & accreditation model (module 81).
 *  Every functional module defined by name, purpose, scope,
 *  owning bodies, users and roles; its workflow, states,
 *  transitions, approvals, exceptions and escalation paths
 *  documented; its entities, data, sensitive fields, source of
 *  truth and retention documented; its interfaces, events,
 *  messages, integrations and idempotency keys documented; and
 *  its user permissions, isolation scope, audit and delegation
 *  documented — before it is accredited. */
@RestController @RequestMapping("/api/module-specs")
public class ModuleSpecController {
  private final ModuleSpecificationRepository specs; private final AuditService audit;
  public ModuleSpecController(ModuleSpecificationRepository specs, AuditService audit) { this.specs=specs; this.audit=audit; }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ModuleSpecification spec(UUID id) {
    return specs.findById(id).orElseThrow(() -> BizException.notFound("spec_not_found","التوصيف غير موجود"));
  }
  private List<String> missing(ModuleSpecification s) {
    return ModuleSpecRules.missingSections(s.getPurpose(), s.getScope(), s.getOwnerOrg(), s.getUsersRoles(),
      s.isWorkflowDocumented(), s.isEntitiesDocumented(), s.isSensitiveFieldsDocumented(),
      s.isInterfacesDocumented(), s.isPermissionsDocumented());
  }

  public record SpecBody(String moduleRef, String purpose, String scope, String ownerOrg, String usersRoles,
                         Boolean workflowDocumented, Boolean entitiesDocumented, Boolean sensitiveFieldsDocumented,
                         Boolean interfacesDocumented, Boolean permissionsDocumented) {}
  @PostMapping("/specifications")
  Map<String,Object> create(@RequestBody SpecBody r) {
    var p = principal();
    var s = specs.save(new ModuleSpecification(UUID.fromString(p.facilityId()), p.governorate(), r.moduleRef(), r.purpose(), r.scope(),
      r.ownerOrg(), r.usersRoles(), Boolean.TRUE.equals(r.workflowDocumented()), Boolean.TRUE.equals(r.entitiesDocumented()),
      Boolean.TRUE.equals(r.sensitiveFieldsDocumented()), Boolean.TRUE.equals(r.interfacesDocumented()), Boolean.TRUE.equals(r.permissionsDocumented())));
    var missing = missing(s);
    return Map.of("ok",true,"specification",s,"complete",missing.isEmpty(),"missing_sections",missing);
  }
  @PostMapping("/specifications/{id}/accredit")
  Map<String,Object> accredit(@PathVariable UUID id) {
    principal(); var s = spec(id);
    var missing = missing(s);
    if (!missing.isEmpty())
      throw BizException.conflict("spec_incomplete","اعتماد يشهد على توصيف كامل — الأقسام الناقصة: " + String.join("، ", missing));
    s.accredit(); specs.save(s);
    audit.record("modulespec.accredited","module_specification",id.toString(),"{}");
    return Map.of("ok",true,"specification",s,"complete",true);
  }
}
