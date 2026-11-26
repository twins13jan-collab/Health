package eg.gov.health.platform.criticalcare;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Critical care & transfer between care levels (module 153).
 *  Admission, transfer and discharge criteria for intensive
 *  care; multidisciplinary assessment and organ support;
 *  high-risk devices and medications; critical-patient
 *  transfer with a fitting team and equipment; case handover
 *  and stability review; and outcomes, infection and
 *  readmission measurement. */
@RestController @RequestMapping("/api/critical-care")
public class CriticalCareController {
  private final IcuLevelTransferRepository transfers; private final AuditService audit;
  public CriticalCareController(IcuLevelTransferRepository transfers, AuditService audit) {
    this.transfers=transfers; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record TransferBody(String patientRef, String fromLevel, String toLevel, Boolean criteriaMet,
                             Boolean stabilityReviewed, Boolean teamEquipped, Boolean handoverDocumented) {}
  @PostMapping("/transfers")
  Map<String,Object> create(@RequestBody TransferBody r) {
    var p = principal();
    var t = transfers.save(new IcuLevelTransfer(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.fromLevel(), r.toLevel(),
      Boolean.TRUE.equals(r.criteriaMet()), Boolean.TRUE.equals(r.stabilityReviewed()), Boolean.TRUE.equals(r.teamEquipped()),
      Boolean.TRUE.equals(r.handoverDocumented())));
    return Map.of("ok",true,"transfer",t);
  }
  @PostMapping("/transfers/{id}/complete")
  Map<String,Object> complete(@PathVariable UUID id) {
    principal();
    var t = transfers.findById(id).orElseThrow(() -> BizException.notFound("transfer_not_found","النقل غير موجود"));
    var blockers = CriticalCareRules.transferBlockers(t.isCriteriaMet(), t.isStabilityReviewed(), t.isTeamEquipped(), t.isHandoverDocumented());
    if (!blockers.isEmpty())
      throw BizException.conflict("transfer_blocked","نقل مريض حرج يقف على: " + String.join("، ", blockers) + " — سرير شاغر حقيقة لوجستية لا قرار سريري");
    t.complete(); transfers.save(t);
    audit.record("criticalcare.transfer.completed","icu_level_transfer",id.toString(),"{}");
    return Map.of("ok",true,"transfer",t);
  }
}
