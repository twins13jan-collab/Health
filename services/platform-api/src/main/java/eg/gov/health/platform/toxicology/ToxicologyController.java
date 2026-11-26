package eg.gov.health.platform.toxicology;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Poison centres & exposures (cycle 254). Intake records facts and shows
 *  uncertainty explicitly; recommendations come only from a named
 *  toxicology consultant backed by a current reference; a mass-exposure
 *  event links its separate cases; referrals are tracked to outcome;
 *  exposure patterns are exported de-identified only. */
@RestController @RequestMapping("/api/toxicology")
public class ToxicologyController {
  private final PoisoningCaseRepository cases; private final SubstanceReferenceRepository references;
  private final ToxicologyConsultationRepository consultations; private final ExposureEventRepository events;
  private final ReferralOutcomeRepository referrals; private final AuditService audit;
  public ToxicologyController(PoisoningCaseRepository cases, SubstanceReferenceRepository references,
                              ToxicologyConsultationRepository consultations, ExposureEventRepository events,
                              ReferralOutcomeRepository referrals, AuditService audit) {
    this.cases=cases; this.references=references; this.consultations=consultations;
    this.events=events; this.referrals=referrals; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private PoisoningCase kase(UUID id) {
    return cases.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","حالة التسمم غير موجودة"));
  }

  public record EventRequest(String locationZone, Integer affectedCount, String suspectedSource) {}
  @PostMapping("/exposure-events")
  Map<String,Object> event(@RequestBody EventRequest r) {
    var p = principal();
    var e = events.save(new ExposureEvent(UUID.fromString(p.facilityId()), p.governorate(), r.locationZone(),
      r.affectedCount()==null?0:r.affectedCount(), r.suspectedSource()));
    audit.record("toxicology.exposure.reported","exposure_event",e.getId().toString(),"{}");
    return Map.of("ok",true,"event",e);
  }

  public record CaseRequest(UUID patientId, UUID exposureEventId, String substanceName, Boolean substanceUnknown,
                            String containerDescription, String estimatedAmount, Boolean amountUncertain, String route,
                            OffsetDateTime exposureTime, Integer ageYears, Double weightKg, String symptoms, String severity) {}
  @PostMapping("/cases")
  Map<String,Object> report(@RequestBody CaseRequest r) {
    var p = principal();
    boolean unknown = Boolean.TRUE.equals(r.substanceUnknown()) || r.substanceName()==null;
    // Unknown substance: describe the container/source — never assume.
    if (!ToxicologyRules.unknownSubstanceDocumented(unknown, r.containerDescription()))
      throw BizException.badRequest("unknown_needs_description","مادة مجهولة: صف العبوة ومصدرها دون افتراض المادة (دورة 254)");
    var c = cases.save(new PoisoningCase(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.exposureEventId(),
      unknown ? null : r.substanceName(), unknown, r.containerDescription(), r.estimatedAmount(),
      Boolean.TRUE.equals(r.amountUncertain()), r.route(), r.exposureTime(), r.ageYears(), r.weightKg(), r.symptoms(), r.severity()));
    audit.record("toxicology.case.reported","poisoning_case",c.getId().toString(),"{}");
    return Map.of("ok",true,"case",c,"uncertainty_shown", Map.of(
      "substance_unknown", unknown, "amount_uncertain", Boolean.TRUE.equals(r.amountUncertain())));
  }
  @PostMapping("/cases/{id}/link-event")
  Map<String,Object> linkEvent(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var c = kase(id);
    String ev = body.get("eventId");
    if (ev==null) throw BizException.badRequest("event_required","معرف حادث التعرض مطلوب");
    var e = events.findById(UUID.fromString(ev)).orElseThrow(() -> BizException.notFound("event_not_found","حادث التعرض غير موجود"));
    c.linkEvent(e.getId()); cases.save(c);
    audit.record("toxicology.case.linked_event","poisoning_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c,"linked_cases", cases.findByExposureEventId(e.getId()).size(),
      "declared_affected", e.getAffectedCount());
  }

  public record ReferenceRequest(String name, String concentration, String packaging, String source, LocalDate reviewDate) {}
  @PostMapping("/references")
  Map<String,Object> reference(@RequestBody ReferenceRequest r) {
    var p = principal();
    if (r.name()==null||r.source()==null||r.reviewDate()==null)
      throw BizException.badRequest("invalid_reference","الاسم والمصدر وتاريخ المراجعة مطلوبة — المراجع موثقة ومؤرخة");
    var ref = references.save(new SubstanceReference(UUID.fromString(p.facilityId()), p.governorate(), r.name(),
      r.concentration(), r.packaging(), r.source(), r.reviewDate()));
    audit.record("toxicology.reference.saved","substance_reference",ref.getId().toString(),"{}");
    return Map.of("ok",true,"reference",ref,"current", ToxicologyRules.referenceCurrent(true, r.reviewDate(), LocalDate.now()));
  }

  public record ConsultationRequest(String consultant, String assessment, String recommendation, UUID referenceId,
                                    Boolean centerUnavailable, String alternativePath) {}
  @PostMapping("/cases/{id}/consultations")
  Map<String,Object> consult(@PathVariable UUID id, @RequestBody ConsultationRequest r) {
    var p = principal(); var c = kase(id);
    // Intake staff record information; they never issue recommendations.
    if (!ToxicologyRules.mayConsult(p.role())) {
      audit.record("toxicology.consult.denied_role","poisoning_case",id.toString(),"{}");
      throw BizException.forbidden("الاستشارة يعتمدها اختصاصي سموم — موظف البلاغ يسجل المعلومات ولا يقدم توصية طبية (دورة 254)");
    }
    boolean unavailable = Boolean.TRUE.equals(r.centerUnavailable());
    if (!ToxicologyRules.recommendationAllowed(unavailable, r.recommendation()))
      throw BizException.badRequest("no_recommendation_when_unavailable","عند تعذر المركز يُسجل عدم التوافر والمسار البديل — ولا تصدر توصية");
    if (unavailable && (r.alternativePath()==null||r.alternativePath().isBlank()))
      throw BizException.badRequest("alternative_path_required","تعذر المركز يتطلب مسارًا بديلًا موثقًا");
    if (!unavailable) {
      if (r.consultant()==null||r.recommendation()==null||r.referenceId()==null)
        throw BizException.badRequest("consultation_incomplete","كل توصية مرتبطة بمستشار ومصدر مرجعي ووقت اتصال (دورة 254)");
      var ref = references.findById(r.referenceId())
        .orElseThrow(() -> BizException.notFound("reference_not_found","المرجع غير موجود"));
      if (!ToxicologyRules.referenceCurrent(ref.isActive(), ref.getReviewDate(), LocalDate.now()))
        throw BizException.badRequest("stale_reference","مرجع غير محدث أو غير نشط لا يدعم توصية — حدّث المرجع أولًا");
    }
    var con = consultations.save(new ToxicologyConsultation(c.getFacilityId(), p.governorate(), id, r.consultant(),
      r.assessment(), unavailable ? null : r.recommendation(), r.referenceId(), unavailable, r.alternativePath()));
    if (!unavailable && "reported".equals(c.getStatus())) { c.setStatus("consulted"); cases.save(c); }
    audit.record("toxicology.consultation.recorded","toxicology_consultation",con.getId().toString(),"{}");
    return Map.of("ok",true,"consultation",con,"center_unavailable",unavailable);
  }

  public record ReferralRequest(String receivingFacility, String transport) {}
  @PostMapping("/cases/{id}/referral")
  Map<String,Object> refer(@PathVariable UUID id, @RequestBody ReferralRequest r) {
    var p = principal(); var c = kase(id);
    if (r.receivingFacility()==null) throw BizException.badRequest("facility_required","جهة الاستقبال مطلوبة");
    var ref = referrals.save(new ReferralOutcome(c.getFacilityId(), p.governorate(), id, r.receivingFacility(), r.transport()));
    c.setStatus("referred"); cases.save(c);
    audit.record("toxicology.referral.initiated","referral_outcome",ref.getId().toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  @PostMapping("/referrals/{id}/outcome")
  Map<String,Object> outcome(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var ref = referrals.findById(id).orElseThrow(() -> BizException.notFound("referral_not_found","الإحالة غير موجودة"));
    if (body.get("outcome")==null) throw BizException.badRequest("outcome_required","نتيجة الإحالة مطلوبة للتتبع");
    ref.recordOutcome(body.get("outcome"), body.get("followUp")); referrals.save(ref);
    audit.record("toxicology.referral.outcome","referral_outcome",id.toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }

  /** De-identified exposure patterns: aggregates only, never patient ids. */
  @GetMapping("/patterns")
  Map<String,Object> patterns() {
    var all = cases.findAllByOrderByCreatedAtDesc();
    Map<String,Long> bySubstance = new TreeMap<>(), byRoute = new TreeMap<>();
    long unknown = 0;
    for (var c : all) {
      if (c.getSubstanceName()==null) unknown++;
      else bySubstance.merge(c.getSubstanceName(), 1L, Long::sum);
      if (c.getRoute()!=null) byRoute.merge(c.getRoute(), 1L, Long::sum);
    }
    return Map.of("ok",true,"total_cases",all.size(),"by_substance",bySubstance,"unknown_substance_cases",unknown,
      "by_route",byRoute,"note","de-identified aggregates — no patient identifiers");
  }

  @GetMapping("/cases")
  Map<String,Object> list() {
    var rows = cases.findAllByOrderByCreatedAtDesc();
    return Map.of("ok",true,"cases",rows,"count",rows.size());
  }
}
