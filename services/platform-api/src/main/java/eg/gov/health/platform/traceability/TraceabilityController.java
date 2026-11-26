package eg.gov.health.platform.traceability;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Inter-module traceability matrix (module 177). Which modules
 *  create and consume each data entity, and who owns it;
 *  responsibility transfers between levels of care; the clinical
 *  record linked to inventory, finance and resources; duplication
 *  and conflicts detected; and the permissions of every transfer
 *  defined. */
@RestController @RequestMapping("/api/traceability")
public class TraceabilityController {
  private final DataFlowRepository flows; private final ResponsibilityTransferRepository transfers; private final AuditService audit;
  public TraceabilityController(DataFlowRepository flows, ResponsibilityTransferRepository transfers, AuditService audit) {
    this.flows=flows; this.transfers=transfers; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record FlowBody(String dataEntity, String producerModule, String consumerModule, String ownerModule, Boolean authorized) {}
  @PostMapping("/flows")
  Map<String,Object> flow(@RequestBody FlowBody r) {
    var p = principal();
    for (var existing : flows.findByDataEntity(r.dataEntity())) {
      if (TraceabilityRules.ownershipConflict(existing.getOwnerModule(), r.ownerModule()))
        throw BizException.conflict("ownership_conflict","كيان بيانات بمالكين يعني بلا مالك — التعارض يُحسم لا يُخزن: المالك المسجل هو " + existing.getOwnerModule());
    }
    var f = flows.save(new DataFlow(UUID.fromString(p.facilityId()), p.governorate(), r.dataEntity(), r.producerModule(), r.consumerModule(), r.ownerModule(), Boolean.TRUE.equals(r.authorized())));
    return Map.of("ok",true,"flow",f);
  }
  public record TransferBody(String dataEntity, String fromLevel, String toLevel, String authorizedRoles) {}
  @PostMapping("/transfers")
  Map<String,Object> transfer(@RequestBody TransferBody r) {
    var p = principal();
    if (!TraceabilityRules.transferAuthorized(r.authorizedRoles()))
      throw BizException.badRequest("roles_required","انتقال مسؤولية بين مستويات رعاية يسمي الأدوار المصرح لها بحمله");
    var t = transfers.save(new ResponsibilityTransfer(UUID.fromString(p.facilityId()), p.governorate(), r.dataEntity(), r.fromLevel(), r.toLevel(), r.authorizedRoles()));
    audit.record("traceability.transfer.defined","responsibility_transfer",t.getId().toString(),"{}");
    return Map.of("ok",true,"transfer",t);
  }
  @GetMapping("/entities/{entity}/owner")
  Map<String,Object> owner(@PathVariable String entity) {
    principal();
    var list = flows.findByDataEntity(entity);
    String owner = list.isEmpty() ? "" : list.get(0).getOwnerModule();
    return Map.of("ok",true,"data_entity",entity,"owner_module",owner,"flows",list.size());
  }
}
