package eg.gov.health.platform.admissionsdeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Admission, entry, transfer & discharge (module 84). The
 *  admission request, its justification, acceptance priority
 *  and required specialty are managed; bed waiting lists,
 *  transfers between departments and facilities and the
 *  receiving side's acceptance are governed; admission
 *  assessment, the daily care plan and medical and nursing
 *  orders run; level-of-care appropriateness and moves into
 *  and out of intensive care are reviewed; discharge is planned
 *  early with medication, follow-up and home-care needs; the
 *  discharge summary carries diagnoses, procedures,
 *  instructions and warnings; discharge against medical advice
 *  and leaving without notice are governed; and readmissions
 *  and their causes are reviewed as care-quality indicators. */
@RestController @RequestMapping("/api/admissions-deep")
public class AdmissionsDeepController {
  private final AdmissionRequestRepository admissions; private final DischargeSummaryRepository summaries;
  private final AmaDischargeRepository amas; private final AuditService audit;
  public AdmissionsDeepController(AdmissionRequestRepository admissions, DischargeSummaryRepository summaries,
                                  AmaDischargeRepository amas, AuditService audit) {
    this.admissions=admissions; this.summaries=summaries; this.amas=amas; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private AdmissionRequest admission(UUID id) {
    return admissions.findById(id).orElseThrow(() -> BizException.notFound("admission_not_found","التنويم غير موجود"));
  }

  public record AdmissionBody(String patientRef, String justification, String priority, String specialty,
                              Boolean transfer, Boolean receivingAcceptance) {}
  @PostMapping("/admissions")
  Map<String,Object> create(@RequestBody AdmissionBody r) {
    var p = principal();
    var a = admissions.save(new AdmissionRequest(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.justification(),
      r.priority(), r.specialty(), Boolean.TRUE.equals(r.transfer()), Boolean.TRUE.equals(r.receivingAcceptance())));
    return Map.of("ok",true,"admission",a);
  }
  @PostMapping("/admissions/{id}/admit")
  Map<String,Object> admit(@PathVariable UUID id) {
    principal(); var a = admission(id);
    if (!AdmissionRules.admittable(a.getJustification(), a.isTransfer(), a.isReceivingAcceptance()))
      throw BizException.conflict("not_admittable","تنويم يحتاج مبررًا — وتحويل يحتاج قبول الجهة المستقبلة: مريض لا يُشحن على أمل");
    a.admit(); admissions.save(a);
    return Map.of("ok",true,"admission",a);
  }
  public record SummaryBody(String diagnoses, String procedures, String instructions, String warnings,
                            String medicationsPlan, String followupPlan) {}
  @PostMapping("/admissions/{id}/discharge-summary")
  Map<String,Object> summary(@PathVariable UUID id, @RequestBody SummaryBody r) {
    var p = principal(); admission(id);
    var gaps = AdmissionRules.summaryGaps(r.diagnoses(), r.procedures(), r.instructions(), r.warnings(), r.medicationsPlan(), r.followupPlan());
    var s = summaries.save(new DischargeSummary(UUID.fromString(p.facilityId()), p.governorate(), id, r.diagnoses(), r.procedures(),
      r.instructions(), r.warnings(), r.medicationsPlan(), r.followupPlan()));
    if (gaps.isEmpty()) { s.finalizeSummary(); summaries.save(s); }
    return Map.of("ok",true,"summary",s,"finalized",gaps.isEmpty(),"gaps",gaps,
      "note", gaps.isEmpty() ? "complete" : "a summary missing its warnings sends the patient home unarmed");
  }
  public record AmaBody(Boolean risksExplained, Boolean signed) {}
  @PostMapping("/admissions/{id}/ama-discharge")
  Map<String,Object> ama(@PathVariable UUID id, @RequestBody AmaBody r) {
    var p = principal(); var a = admission(id);
    boolean documented = AdmissionRules.amaDocumented(Boolean.TRUE.equals(r.risksExplained()), Boolean.TRUE.equals(r.signed()));
    var d = amas.save(new AmaDischarge(UUID.fromString(p.facilityId()), p.governorate(), id,
      Boolean.TRUE.equals(r.risksExplained()), Boolean.TRUE.equals(r.signed())));
    if (documented) { a.discharge(); admissions.save(a); }
    return Map.of("ok",true,"ama_discharge",d,"documented",documented,"discharged",documented);
  }
}
