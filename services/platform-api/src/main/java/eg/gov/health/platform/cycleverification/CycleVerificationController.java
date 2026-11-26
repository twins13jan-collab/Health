package eg.gov.health.platform.cycleverification;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** End-to-end service-cycle completeness verification
 *  (module 180). The start of a service, its completion and
 *  the owner of every transition are defined; request, triage,
 *  approval, execution, result and follow-up verified; the
 *  medication, stock, invoice, cost and journal cycle checked;
 *  referral acceptance, service delivery and result return
 *  verified; stuck, stalled and cancelled cases managed; and a
 *  screen or an API never accepted as evidence that the
 *  process completed. */
@RestController @RequestMapping("/api/cycle-verification")
public class CycleVerificationController {
  private final CycleVerificationRepository verifications; private final AuditService audit;
  public CycleVerificationController(CycleVerificationRepository verifications, AuditService audit) {
    this.verifications=verifications; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record VerBody(String cycleRef, String instanceRef, Boolean requestDone, Boolean triageDone, Boolean approvalDone,
                        Boolean executionDone, Boolean resultDone, Boolean followupDone, String evidenceRefs) {}
  @PostMapping("/verifications")
  Map<String,Object> create(@RequestBody VerBody r) {
    var p = principal();
    var v = verifications.save(new CycleVerification(UUID.fromString(p.facilityId()), p.governorate(), r.cycleRef(), r.instanceRef(),
      Boolean.TRUE.equals(r.requestDone()), Boolean.TRUE.equals(r.triageDone()), Boolean.TRUE.equals(r.approvalDone()),
      Boolean.TRUE.equals(r.executionDone()), Boolean.TRUE.equals(r.resultDone()), Boolean.TRUE.equals(r.followupDone()), r.evidenceRefs()));
    return Map.of("ok",true,"verification",v);
  }
  @GetMapping("/verifications/{id}/completeness")
  Map<String,Object> completeness(@PathVariable UUID id) {
    principal();
    var v = verifications.findById(id).orElseThrow(() -> BizException.notFound("verification_not_found","التحقق غير موجود"));
    var missing = CycleVerificationRules.missingStages(v.isRequestDone(), v.isTriageDone(), v.isApprovalDone(),
      v.isExecutionDone(), v.isResultDone(), v.isFollowupDone());
    boolean complete = CycleVerificationRules.complete(v.isRequestDone(), v.isTriageDone(), v.isApprovalDone(),
      v.isExecutionDone(), v.isResultDone(), v.isFollowupDone(), v.getEvidenceRefs());
    return Map.of("ok",true,"verification",v,"complete",complete,"missing_stages",missing,
      "evidence_present",v.getEvidenceRefs() != null && !v.getEvidenceRefs().isBlank(),
      "note","a screen that rendered or an API that returned 200 is not evidence the process completed");
  }
}
