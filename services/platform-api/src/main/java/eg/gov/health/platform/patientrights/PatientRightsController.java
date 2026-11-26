package eg.gov.health.platform.patientrights;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Complaints, investigation, resolution & appeal (cycle 232).
 *  Reception registers and never alters content; imminent risk
 *  escalates to safety at once; the investigator works on the minimum
 *  necessary with every access logged; a conflict of interest
 *  reassigns the investigation; governance separates investigation
 *  from decision; closure needs a resolution plus a root cause linked
 *  to a corrective action; appeals go to an independent review body;
 *  third-party data disclosure passes privacy and legal review; and
 *  the patient-rights office guards against retaliation. */
@RestController @RequestMapping("/api/patient-complaints")
public class PatientRightsController {
  private final PatientComplaintRepository complaints; private final ComplaintInvestigationRepository investigations;
  private final ComplaintEvidenceItemRepository evidence; private final ComplaintAccessLogRepository accessLog;
  private final ComplaintResolutionRepository resolutions; private final ComplaintAppealRepository appeals;
  private final DisclosureReviewRepository disclosures; private final AuditService audit;
  public PatientRightsController(PatientComplaintRepository complaints, ComplaintInvestigationRepository investigations,
                                 ComplaintEvidenceItemRepository evidence, ComplaintAccessLogRepository accessLog,
                                 ComplaintResolutionRepository resolutions, ComplaintAppealRepository appeals,
                                 DisclosureReviewRepository disclosures, AuditService audit) {
    this.complaints=complaints; this.investigations=investigations; this.evidence=evidence; this.accessLog=accessLog;
    this.resolutions=resolutions; this.appeals=appeals; this.disclosures=disclosures; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private PatientComplaint complaint(UUID id) {
    return complaints.findById(id).orElseThrow(() -> BizException.notFound("complaint_not_found","الشكوى غير موجودة"));
  }

  public record ComplaintRequest(String complainantName, Boolean anonymous, String representation, String contact,
                                 Boolean identityVerified, String content, String category, Boolean imminentRisk, String safetyCaseRef) {}
  @PostMapping
  Map<String,Object> register(@RequestBody ComplaintRequest r) {
    var p = principal();
    if (r.content()==null||r.content().isBlank()) throw BizException.badRequest("content_required","مضمون الشكوى مطلوب كما ورد");
    boolean anonymous = Boolean.TRUE.equals(r.anonymous());
    if (!ComplaintRules.anonymousValid(anonymous, r.complainantName()))
      throw BizException.badRequest("identity_or_anonymous","إما هوية مقدم الشكوى أو تقديمها مجهولة صراحة — لا اختلاق هوية");
    boolean imminent = Boolean.TRUE.equals(r.imminentRisk());
    if (imminent && (r.safetyCaseRef()==null||r.safetyCaseRef().isBlank()))
      throw BizException.badRequest("safety_ref_required","الخطر الوشيك يصعد لمسار السلامة فورًا — مرجع حالة السلامة مطلوب");
    var c = new PatientComplaint(UUID.fromString(p.facilityId()), p.governorate(), anonymous ? null : r.complainantName(),
      anonymous, r.representation(), r.contact(), Boolean.TRUE.equals(r.identityVerified()), r.content(), r.category(), imminent);
    if (imminent) c.escalateSafety(r.safetyCaseRef());
    complaints.save(c);
    audit.record("complaint.registered","patient_complaint",c.getId().toString(),"{}");
    return Map.of("ok",true,"complaint",c,"content_immutable",true,
      "safety_escalated",c.isImminentRisk(),"note","reception registers the complaint verbatim and never alters its content");
  }
  @PostMapping("/{id}/assign")
  Map<String,Object> assign(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal(); var c = complaint(id);
    if (body.get("investigator")==null) throw BizException.badRequest("investigator_required","المحقق مطلوب");
    var inv = investigations.save(new ComplaintInvestigation(c.getFacilityId(), p.governorate(), id, body.get("investigator"), body.get("reassignedFrom")));
    c.setStatus("investigating"); complaints.save(c);
    audit.record("complaint.investigation.assigned","complaint_investigation",inv.getId().toString(),"{}");
    return Map.of("ok",true,"investigation",inv);
  }
  @PostMapping("/investigations/{id}/conflict")
  Map<String,Object> conflict(@PathVariable UUID id) {
    var inv = investigations.findById(id).orElseThrow(() -> BizException.notFound("investigation_not_found","التحقيق غير موجود"));
    inv.declareConflict(); investigations.save(inv);
    audit.record("complaint.investigation.conflict","complaint_investigation",id.toString(),"{}");
    return Map.of("ok",true,"investigation",inv,"note","conflict of interest declared — the investigation must be reassigned");
  }
  @PostMapping("/investigations/{id}/complete")
  Map<String,Object> completeInvestigation(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var inv = investigations.findById(id).orElseThrow(() -> BizException.notFound("investigation_not_found","التحقيق غير موجود"));
    if ("reassigned".equals(inv.getStatus()))
      throw BizException.conflict("reassigned","تحقيق أعيد إسناده لتضارب مصالح لا يكمله المحقق السابق");
    inv.complete(body.get("timeline")); investigations.save(inv);
    audit.record("complaint.investigation.completed","complaint_investigation",id.toString(),"{}");
    return Map.of("ok",true,"investigation",inv);
  }
  public record EvidenceRequest(String description, String contentHash) {}
  @PostMapping("/{id}/evidence")
  Map<String,Object> addEvidence(@PathVariable UUID id, @RequestBody EvidenceRequest r) {
    var p = principal(); var c = complaint(id);
    if (r.contentHash()==null) throw BizException.badRequest("hash_required","الأدلة تحفظ ببصمة تحقق");
    var e = evidence.save(new ComplaintEvidenceItem(c.getFacilityId(), p.governorate(), id, r.description(), r.contentHash(), p.email()));
    audit.record("complaint.evidence.added","complaint_evidence",e.getId().toString(),"{}");
    return Map.of("ok",true,"evidence",e);
  }
  /** Minimum-necessary access: every read of the file is itself logged. */
  @GetMapping("/{id}/file")
  Map<String,Object> file(@PathVariable UUID id, @RequestParam String purpose) {
    var p = principal(); var c = complaint(id);
    accessLog.save(new ComplaintAccessLog(c.getFacilityId(), p.governorate(), id, p.email(), purpose));
    audit.record("complaint.file.accessed","patient_complaint",id.toString(),"{}");
    return Map.of("ok",true,"complaint",c,
      "evidence",evidence.findAll().stream().filter(e -> id.equals(eComplaint(e))).toList(),
      "access_log",accessLog.findByComplaintIdOrderByCreatedAtAsc(id),
      "investigations",investigations.findByComplaintIdOrderByCreatedAtAsc(id));
  }
  private UUID eComplaint(ComplaintEvidenceItem e) { return e.getComplaintId(); }
  public record ResolutionRequest(String decision, String basis) {}
  @PostMapping("/{id}/resolution")
  Map<String,Object> resolution(@PathVariable UUID id, @RequestBody ResolutionRequest r) {
    var p = principal(); var c = complaint(id);
    String investigator = investigations.findByComplaintIdOrderByCreatedAtAsc(id).stream()
      .filter(i -> !"reassigned".equals(i.getStatus())).map(ComplaintInvestigation::getInvestigator).findFirst().orElse(null);
    // Governance: investigation is separated from decision.
    if (!ComplaintRules.deciderIndependent(investigator, p.email()))
      throw BizException.forbidden("فصل التحقيق عن القرار — لا يقرر المحقق في تحقيقه");
    var res = resolutions.save(new ComplaintResolution(c.getFacilityId(), p.governorate(), id, r.decision(), r.basis(), p.email()));
    c.setStatus("resolved"); complaints.save(c);
    audit.record("complaint.resolved","complaint_resolution",res.getId().toString(),"{}");
    return Map.of("ok",true,"resolution",res);
  }
  public record CloseRequest(String rootCause, String correctiveAction) {}
  @PostMapping("/{id}/close")
  Map<String,Object> close(@PathVariable UUID id, @RequestBody CloseRequest r) {
    var p = principal(); var c = complaint(id);
    String decision = resolutions.findByComplaintIdOrderByCreatedAtDesc(id).stream().findFirst().map(ComplaintResolution::getDecision).orElse(null);
    if (!ComplaintRules.closable(decision, r.rootCause(), r.correctiveAction()))
      throw BizException.badRequest("closure_requirements","الإغلاق يتطلب قرارًا وسببًا جذريًا مرتبطًا بإجراء تصحيحي");
    c.closeWith(r.rootCause(), r.correctiveAction(), p.email()); complaints.save(c);
    audit.record("complaint.closed","patient_complaint",id.toString(),"{}");
    return Map.of("ok",true,"complaint",c,"retaliation_guard","patient-rights office monitors for retaliation against the complainant");
  }
  @PostMapping("/{id}/appeals")
  Map<String,Object> appeal(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal(); var c = complaint(id);
    var a = appeals.save(new ComplaintAppeal(c.getFacilityId(), p.governorate(), id, body.get("grounds")));
    c.setStatus("appealed"); complaints.save(c);
    audit.record("complaint.appeal.filed","complaint_appeal",a.getId().toString(),"{}");
    return Map.of("ok",true,"appeal",a,"note","appeals are decided by an independent review body");
  }
  @PostMapping("/appeals/{id}/review")
  Map<String,Object> reviewAppeal(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal();
    var a = appeals.findById(id).orElseThrow(() -> BizException.notFound("appeal_not_found","التظلم غير موجود"));
    if (!Set.of("independent_review_board","ombudsman","admin").contains(p.role()))
      throw BizException.forbidden("جهة مراجعة مستقلة تعتمد التظلم — لا الجهة صاحبة القرار الأصلي");
    if (body.get("outcome")==null||!Set.of("upheld","overturned").contains(body.get("outcome")))
      throw BizException.badRequest("outcome_required","نتيجة التظلم: upheld أو overturned");
    a.review(p.email(), body.get("outcome")); appeals.save(a);
    audit.record("complaint.appeal.reviewed","complaint_appeal",id.toString(),"{}");
    return Map.of("ok",true,"appeal",a);
  }
  public record DisclosureRequest(String requester, String dataSubject) {}
  @PostMapping("/{id}/disclosure-reviews")
  Map<String,Object> disclosure(@PathVariable UUID id, @RequestBody DisclosureRequest r) {
    var p = principal(); var c = complaint(id);
    var d = disclosures.save(new DisclosureReview(c.getFacilityId(), p.governorate(), id, r.requester(), r.dataSubject()));
    audit.record("complaint.disclosure.requested","disclosure_review",d.getId().toString(),"{}");
    return Map.of("ok",true,"disclosure",d,"note","third-party data disclosure requires privacy AND legal approval");
  }
  @PostMapping("/disclosure-reviews/{id}/review")
  Map<String,Object> reviewDisclosure(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var d = disclosures.findById(id).orElseThrow(() -> BizException.notFound("disclosure_not_found","طلب الكشف غير موجود"));
    d.review(body.get("privacyReview"), body.get("legalReview")); disclosures.save(d);
    audit.record("complaint.disclosure.reviewed","disclosure_review",id.toString(),"{}");
    return Map.of("ok",true,"disclosure",d,"disclosable", "approved".equals(dStatus(d)));
  }
  private String dStatus(DisclosureReview d) { return d.getStatus(); }
}
