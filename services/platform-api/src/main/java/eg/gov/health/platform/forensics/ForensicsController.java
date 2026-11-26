package eg.gov.health.platform.forensics;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Forensic medicine & medico-legal reports (module 71).
 *  Examination requests, medico-legal reports and the chain of
 *  custody of samples and evidence are managed apart from the
 *  ordinary clinical record. Requests from judicial and official
 *  bodies are verified for jurisdiction and mandate before any
 *  examination; signatures, approvals, access and delivery are
 *  documented end to end. */
@RestController @RequestMapping("/api/forensics")
public class ForensicsController {
  private final ForensicCaseRepository cases; private final ForensicEvidenceItemRepository evidence;
  private final ChainOfCustodyEventRepository custody; private final ForensicReportRepository reports; private final AuditService audit;
  public ForensicsController(ForensicCaseRepository cases, ForensicEvidenceItemRepository evidence,
                             ChainOfCustodyEventRepository custody, ForensicReportRepository reports, AuditService audit) {
    this.cases=cases; this.evidence=evidence; this.custody=custody; this.reports=reports; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (!ForensicRules.forensicAccess(p.role())) throw BizException.forbidden("ملف الطب الشرعي منفصل عن الملف العلاجي — صلاحيات شرعية فقط");
    return p;
  }
  private ForensicCase theCase(UUID id) {
    return cases.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","القضية غير موجودة"));
  }

  public record CaseRequest(String caseRef, String requestingAuthority, String mandateRef, Boolean authorityVerified) {}
  @PostMapping("/cases")
  Map<String,Object> open(@RequestBody CaseRequest r) {
    var p = principal();
    var c = cases.save(new ForensicCase(UUID.fromString(p.facilityId()), p.governorate(), r.caseRef(), r.requestingAuthority(), r.mandateRef(),
      Boolean.TRUE.equals(r.authorityVerified())));
    audit.record("forensic.case.received","forensic_case",c.getId().toString(),"{}");
    return Map.of("ok",true,"case",c);
  }
  @PostMapping("/cases/{id}/begin-examination")
  Map<String,Object> begin(@PathVariable UUID id) {
    principal(); var c = theCase(id);
    if (!ForensicRules.examinationPermitted(c.isAuthorityVerified()))
      throw BizException.conflict("authority_unverified","لا فحص شرعيًا قبل التحقق من اختصاص الجهة القضائية الطالبة وتفويضها");
    c.setStatus("in_examination"); cases.save(c);
    return Map.of("ok",true,"case",c);
  }
  public record EvidenceRequest(String itemDescription, String sealNo) {}
  @PostMapping("/cases/{id}/evidence")
  Map<String,Object> addEvidence(@PathVariable UUID id, @RequestBody EvidenceRequest r) {
    var p = principal(); theCase(id);
    var e = evidence.save(new ForensicEvidenceItem(UUID.fromString(p.facilityId()), p.governorate(), id, r.itemDescription(), r.sealNo()));
    custody.save(new ChainOfCustodyEvent(UUID.fromString(p.facilityId()), p.governorate(), e.getId(), "received", "تقديم الجهة الطالبة", "مخزن الأدلة الشرعية", p.email()));
    audit.record("forensic.evidence.received","forensic_evidence_item",e.getId().toString(),"{}");
    return Map.of("ok",true,"evidence",e,"chain","custody chain opened with a sealed receipt event");
  }
  public record TransferRequest(String fromParty, String toParty) {}
  @PostMapping("/evidence/{eid}/transfer")
  Map<String,Object> transfer(@PathVariable UUID eid, @RequestBody TransferRequest r) {
    var p = principal();
    var e = evidence.findById(eid).orElseThrow(() -> BizException.notFound("evidence_not_found","الدليل غير موجود"));
    if (!ForensicRules.transferPermitted(e.getStatus(), r.fromParty(), r.toParty()))
      throw BizException.conflict("custody_break","نقل الدليل يتطلب حيازة قائمة وطرفين مسميين — سلسلة الحيازة لا تنكسر");
    custody.save(new ChainOfCustodyEvent(UUID.fromString(p.facilityId()), p.governorate(), eid, "transferred", r.fromParty(), r.toParty(), p.email()));
    e.setStatus("transferred"); evidence.save(e);
    audit.record("forensic.evidence.transferred","forensic_evidence_item",eid.toString(),"{}");
    return Map.of("ok",true,"evidence",e);
  }
  public record ReportRequest(String reportType, String countersignedBy) {}
  @PostMapping("/cases/{id}/reports")
  Map<String,Object> report(@PathVariable UUID id, @RequestBody ReportRequest r) {
    var p = principal(); theCase(id);
    var rep = reports.save(new ForensicReport(UUID.fromString(p.facilityId()), p.governorate(), id, r.reportType(), p.email(), r.countersignedBy()));
    audit.record("forensic.report.signed","forensic_report",rep.getId().toString(),"{}");
    return Map.of("ok",true,"report",rep);
  }
  public record ReleaseRequest(String releasedTo) {}
  @PostMapping("/cases/{id}/reports/{rid}/release")
  Map<String,Object> release(@PathVariable UUID id, @PathVariable UUID rid, @RequestBody ReleaseRequest r) {
    principal(); var c = theCase(id);
    var rep = reports.findById(rid).orElseThrow(() -> BizException.notFound("report_not_found","التقرير غير موجود"));
    if (!ForensicRules.reportReleasePermitted(rep.getSignedBy(), rep.getCountersignedBy(), r.releasedTo(), c.getRequestingAuthority()))
      throw BizException.conflict("release_blocked","إفراج التقرير الطبي القانوني يتطلب توقيع الفاحص واعتمادًا ثانيًا مختلفًا، والتسليم للجهة الطالبة المتحقق منها فقط");
    rep.release(r.releasedTo()); reports.save(rep);
    c.setStatus("reported"); cases.save(c);
    audit.record("forensic.report.released","forensic_report",rid.toString(),"{}");
    return Map.of("ok",true,"report",rep,"released_to",r.releasedTo());
  }
}
