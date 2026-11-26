package eg.gov.health.platform.pathology;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Pathology, histology & cytology (cycle 240). The specimen is tracked
 *  from order to report; identity is never guessed — unverified
 *  specimens are quarantined; preliminary is not final; a signed final
 *  report is never edited, only amended by a new version that notifies
 *  previous recipients; dissenting opinions are preserved side by side. */
@RestController @RequestMapping("/api/pathology")
public class PathologyController {
  private final PathologyOrderRepository orders; private final SpecimenAccessionRepository accessions;
  private final BlockSlideRepository slides; private final PathologyReportRepository reports;
  private final PathologySecondOpinionRepository opinions; private final AuditService audit;
  public PathologyController(PathologyOrderRepository orders, SpecimenAccessionRepository accessions,
                             BlockSlideRepository slides, PathologyReportRepository reports,
                             PathologySecondOpinionRepository opinions, AuditService audit) {
    this.orders=orders; this.accessions=accessions; this.slides=slides; this.reports=reports;
    this.opinions=opinions; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private SpecimenAccession accession(UUID id) {
    return accessions.findById(id).orElseThrow(() -> BizException.notFound("accession_not_found","العينة غير موجودة"));
  }

  public record OrderRequest(UUID patientId, String requestingPhysician, String reason, String site, String specimenType, String urgency) {}
  @PostMapping("/orders")
  Map<String,Object> order(@RequestBody OrderRequest r) {
    var p = principal();
    if (r.patientId()==null||r.specimenType()==null) throw BizException.badRequest("invalid_order","المريض ونوع العينة مطلوبان");
    var o = orders.save(new PathologyOrder(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.requestingPhysician(), r.reason(), r.site(), r.specimenType(), r.urgency()));
    audit.record("pathology.order.created","pathology_order",o.getId().toString(),"{}");
    return Map.of("ok",true,"order",o);
  }

  public record AccessionRequest(String accessionNumber, String containerType, String fixative, OffsetDateTime collectedAt, Boolean identityVerified) {}
  @PostMapping("/orders/{id}/accessions")
  Map<String,Object> receive(@PathVariable UUID id, @RequestBody AccessionRequest r) {
    var p = principal();
    var o = orders.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","الطلب غير موجود"));
    if (r.accessionNumber()==null) throw BizException.badRequest("accession_required","رقم العينة مطلوب");
    boolean verified = Boolean.TRUE.equals(r.identityVerified());
    var a = accessions.save(new SpecimenAccession(o.getFacilityId(), p.governorate(), id, r.accessionNumber(),
      r.containerType(), r.fixative(), r.collectedAt(), verified));
    if ("ordered".equals(o.getStatus())) { o.setStatus("received"); orders.save(o); }
    audit.record("pathology.specimen.received","specimen_accession",a.getId().toString(),"{}");
    return Map.of("ok",true,"accession",a,"quarantined", "quarantined".equals(a.getStatus()),
      "note", verified ? "" : "unidentified/mismatched specimen quarantined — verify, never guess");
  }
  @PostMapping("/accessions/{id}/verify-identity")
  Map<String,Object> verify(@PathVariable UUID id) {
    var a = accession(id); a.verifyIdentity(); accessions.save(a);
    audit.record("pathology.specimen.identity_verified","specimen_accession",id.toString(),"{}");
    return Map.of("ok",true,"accession",a);
  }
  @PostMapping("/accessions/{id}/accept")
  Map<String,Object> accept(@PathVariable UUID id) {
    var a = accession(id);
    if (!"received".equals(a.getStatus()))
      throw BizException.conflict("not_acceptable","لا قبول لعينة محجورة أو موسومة القصور — تحقق من الهوية أولًا");
    a.accept(); accessions.save(a);
    audit.record("pathology.specimen.accepted","specimen_accession",id.toString(),"{}");
    return Map.of("ok",true,"accession",a);
  }
  @PostMapping("/accessions/{id}/insufficient")
  Map<String,Object> insufficient(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var a = accession(id);
    if (!PathologyRules.insufficiencyDocumented(body.get("note")))
      throw BizException.badRequest("insufficiency_note_required","قصور العينة يوثق وطلب إعادة الأخذ قرار سريري (دورة 240)");
    a.markInsufficient(body.get("note")); accessions.save(a);
    audit.record("pathology.specimen.insufficient","specimen_accession",id.toString(),"{}");
    return Map.of("ok",true,"accession",a,"recollection_decision","clinical — request a new specimen per the treating team");
  }

  public record SlideRequest(String blockCode, String slideCode, String stain, String imageRef) {}
  @PostMapping("/accessions/{id}/slides")
  Map<String,Object> slide(@PathVariable UUID id, @RequestBody SlideRequest r) {
    var p = principal(); var a = accession(id);
    if (!PathologyRules.processable(a.getStatus()))
      throw BizException.conflict("specimen_not_accepted","التحضير يتطلب عينة مقبولة موثقة الهوية");
    var s = slides.save(new BlockSlide(a.getFacilityId(), p.governorate(), id, r.blockCode(), r.slideCode(), r.stain(), r.imageRef()));
    s.ready(); slides.save(s);
    audit.record("pathology.slide.ready","block_slide",s.getId().toString(),"{}");
    return Map.of("ok",true,"slide",s);
  }

  public record ReportRequest(String kind, String reportText, String diagnosis, String classification, String certainty,
                              String author, String reviewer, Boolean signed) {}
  @PostMapping("/accessions/{id}/reports")
  Map<String,Object> report(@PathVariable UUID id, @RequestBody ReportRequest r) {
    var p = principal(); var a = accession(id);
    if (!PathologyRules.processable(a.getStatus()))
      throw BizException.conflict("specimen_not_accepted","لا تقرير على عينة غير مقبولة");
    if (r.kind()==null||!(r.kind().equals("preliminary")||r.kind().equals("final")))
      throw BizException.badRequest("invalid_kind","نوع التقرير: أولي أو نهائي — والتعديل يكون بإضافة لاحقة لا بتحرير النهائي");
    if (!PathologyRules.finalReportValid(r.kind(), Boolean.TRUE.equals(r.signed()), r.author()))
      throw BizException.badRequest("final_needs_signature","التقرير النهائي يعتمده اختصاصي علم أمراض بتوقيعه وهويته (دورة 240)");
    int version = reports.findByAccessionIdOrderByCreatedAtDesc(id).size() + 1;
    var rep = reports.save(new PathologyReport(a.getFacilityId(), p.governorate(), id, r.kind(), r.reportText(),
      r.diagnosis(), r.classification(), r.certainty(), r.author(), r.reviewer(), Boolean.TRUE.equals(r.signed()), version, null, null));
    audit.record("pathology.report."+r.kind(),"pathology_report",rep.getId().toString(),"{}");
    long tatHours = Duration.between(a.getReceivedAt(), OffsetDateTime.now()).toHours();
    return Map.of("ok",true,"report",rep,"turnaround_hours",tatHours);
  }

  /** Amendment: the signed original is never edited — a new addendum
   *  version supersedes it and previous recipients must be notified. */
  public record AmendRequest(String reportText, String diagnosis, String author, String amendmentReason) {}
  @PostMapping("/reports/{id}/amend")
  Map<String,Object> amend(@PathVariable UUID id, @RequestBody AmendRequest r) {
    var p = principal();
    var orig = reports.findById(id).orElseThrow(() -> BizException.notFound("report_not_found","التقرير غير موجود"));
    if (!"current".equals(orig.getStatus())) throw BizException.conflict("already_superseded","التقرير ليس النسخة الحالية");
    if (!PathologyRules.amendmentValid(r.amendmentReason()))
      throw BizException.badRequest("amendment_reason_required","تعديل تقرير معتمد يكون بإصدار إضافة/تصحيح بسبب موثق (دورة 240)");
    var add = reports.save(new PathologyReport(orig.getFacilityId(), p.governorate(), orig.getAccessionId(), "addendum",
      r.reportText(), r.diagnosis(), null, null, r.author(), null, true, orig.getVersion()+1, id, r.amendmentReason()));
    orig.supersede(); reports.save(orig);
    audit.record("pathology.report.amended","pathology_report",add.getId().toString(),"{}");
    return Map.of("ok",true,"addendum",add,"superseded_report",id,"notify_previous_recipients",true);
  }

  public record OpinionRequest(String pathologist, String opinion, String reviewDecision) {}
  @PostMapping("/reports/{id}/second-opinion")
  Map<String,Object> opinion(@PathVariable UUID id, @RequestBody OpinionRequest r) {
    var p = principal();
    var rep = reports.findById(id).orElseThrow(() -> BizException.notFound("report_not_found","التقرير غير موجود"));
    if (r.pathologist()==null||r.opinion()==null) throw BizException.badRequest("invalid_opinion","الاختصاصي ورأيه مطلوبان");
    var op = opinions.save(new PathologySecondOpinion(rep.getFacilityId(), p.governorate(), id, r.pathologist(), r.opinion(), r.reviewDecision()));
    audit.record("pathology.second_opinion.recorded","pathology_second_opinion",op.getId().toString(),"{}");
    return Map.of("ok",true,"opinion",op,"note","both opinions preserved — the review decision governs, nothing is erased");
  }

  @GetMapping("/orders")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = orders.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"orders",rows,"count",rows.size());
  }
}
