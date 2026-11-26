package eg.gov.health.platform.auditcompliance;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Audit, compliance & anti-abuse (module 118). Audit plans,
 *  evidence and findings; conflicts of interest and
 *  disclosures; fraud signals without automatic judgments;
 *  compliance cases and corrective actions; audit records
 *  protected and separated from operations; regulator requests
 *  handled; and security and privacy incident disclosure. */
@RestController @RequestMapping("/api/audit-compliance")
public class AuditComplianceController {
  private final AuditFindingRepository findings; private final ConflictDisclosureRepository disclosures;
  private final FraudSignalRepository signals; private final AuditService audit;
  public AuditComplianceController(AuditFindingRepository findings, ConflictDisclosureRepository disclosures,
                                   FraudSignalRepository signals, AuditService audit) {
    this.findings=findings; this.disclosures=disclosures; this.signals=signals; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record FindingBody(String auditRef, String severity, String evidenceRef, String correctiveAction, Boolean verifiedClosed) {}
  @PostMapping("/findings")
  Map<String,Object> finding(@RequestBody FindingBody r) {
    var p = principal();
    var f = findings.save(new AuditFinding(UUID.fromString(p.facilityId()), p.governorate(), r.auditRef(), r.severity(),
      r.evidenceRef(), r.correctiveAction(), Boolean.TRUE.equals(r.verifiedClosed())));
    return Map.of("ok",true,"finding",f);
  }
  @PostMapping("/findings/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal();
    var f = findings.findById(id).orElseThrow(() -> BizException.notFound("finding_not_found","الملاحظة غير موجودة"));
    if (!ComplianceRules.findingClosable(f.getEvidenceRef(), f.getCorrectiveAction(), f.isVerifiedClosed()))
      throw BizException.conflict("not_closable","ملاحظة تدقيق لا تُغلق قبل دليل وإجراء تصحيحي وإغلاق موثق — ملاحظة تُغلق بوعد تعيد فتح نفسها عند الحادث التالي");
    f.close(); findings.save(f);
    return Map.of("ok",true,"finding",f);
  }
  public record DisclosureBody(String personRef, String interest, Boolean disclosed, String managementPlan) {}
  @PostMapping("/conflict-disclosures")
  Map<String,Object> disclosure(@RequestBody DisclosureBody r) {
    var p = principal();
    var d = disclosures.save(new ConflictDisclosure(UUID.fromString(p.facilityId()), p.governorate(), r.personRef(), r.interest(),
      Boolean.TRUE.equals(r.disclosed()), r.managementPlan()));
    return Map.of("ok",true,"disclosure",d,"may_act_on_related_decisions",d.isDisclosed(),
      "note","an undisclosed conflict bars acting on related decisions until disclosed and managed");
  }
  public record SignalBody(String signalRef) {}
  @PostMapping("/fraud-signals")
  Map<String,Object> signal(@RequestBody SignalBody r) {
    var p = principal();
    var s = signals.save(new FraudSignal(UUID.fromString(p.facilityId()), p.governorate(), r.signalRef()));
    return Map.of("ok",true,"signal",s,"status",ComplianceRules.signalStatus(s.isHumanReviewed(), s.getHumanOutcome()),
      "note","a signal is not a judgment — no automatic verdict exists in this system");
  }
  public record DecideBody(String outcome) {}
  @PostMapping("/fraud-signals/{id}/human-decision")
  Map<String,Object> decide(@PathVariable UUID id, @RequestBody DecideBody r) {
    principal();
    var s = signals.findById(id).orElseThrow(() -> BizException.notFound("signal_not_found","الإشارة غير موجودة"));
    s.decide(r.outcome()); signals.save(s);
    audit.record("compliance.fraud.human_decision","fraud_signal",id.toString(),"{}");
    return Map.of("ok",true,"signal",s,"status",ComplianceRules.signalStatus(true, r.outcome()));
  }
}
