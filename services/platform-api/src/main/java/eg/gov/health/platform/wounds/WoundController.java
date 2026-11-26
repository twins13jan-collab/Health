package eg.gov.health.platform.wounds;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Burns, wounds & advanced tissue care (cycle 250). Healing is judged
 *  from serial area measurements against a baseline; suspected infection
 *  escalates on its own; dressing materials trace to their lot and
 *  expired/recalled lots are blocked; clinical images require a consent
 *  basis and are viewable only by restricted roles; burn resuscitation
 *  is computed by Parkland and tracked against actuals. */
@RestController @RequestMapping("/api/wounds")
public class WoundController {
  private final WoundCaseRepository cases; private final WoundAssessmentRepository assessments;
  private final ClinicalImageRepository images; private final DressingEpisodeRepository dressings;
  private final BurnResuscitationRecordRepository resuscitation; private final AuditService audit;
  public WoundController(WoundCaseRepository cases, WoundAssessmentRepository assessments,
                         ClinicalImageRepository images, DressingEpisodeRepository dressings,
                         BurnResuscitationRecordRepository resuscitation, AuditService audit) {
    this.cases=cases; this.assessments=assessments; this.images=images;
    this.dressings=dressings; this.resuscitation=resuscitation; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private WoundCase kase(UUID id) {
    return cases.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","حالة الجرح/الحرق غير موجودة"));
  }

  public record CaseRequest(UUID patientId, String cause, String site, Integer durationDays, String classification,
                            Integer burnDegree, Double tbsaPercent) {}
  @PostMapping("/cases")
  Map<String,Object> open(@RequestBody CaseRequest r) {
    var p = principal();
    if (r.patientId()==null||r.classification()==null) throw BizException.badRequest("invalid_case","المريض والتصنيف مطلوبان");
    if (r.tbsaPercent()!=null && !WoundRules.tbsaValid(r.tbsaPercent()))
      throw BizException.badRequest("tbsa_invalid","نسبة مساحة الحرق TBSA بين 0 و100");
    if (r.burnDegree()!=null && (r.burnDegree()<1||r.burnDegree()>3))
      throw BizException.badRequest("degree_invalid","درجة الحرق 1–3");
    var c = cases.save(new WoundCase(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.cause(),
      r.site(), r.durationDays(), r.classification(), r.burnDegree(), r.tbsaPercent()));
    audit.record("wound.case.opened","wound_case",c.getId().toString(),"{}");
    return Map.of("ok",true,"case",c);
  }

  public record AssessmentRequest(Double lengthCm, Double widthCm, Double depthCm, String tissueType, String exudate,
                                  Integer painScore, Boolean infectionSuspected, String assessedBy) {}
  @PostMapping("/cases/{id}/assessments")
  Map<String,Object> assess(@PathVariable UUID id, @RequestBody AssessmentRequest r) {
    var p = principal(); var c = kase(id);
    var a = assessments.save(new WoundAssessment(c.getFacilityId(), p.governorate(), id, r.lengthCm(), r.widthCm(),
      r.depthCm(), r.tissueType(), r.exudate(), r.painScore(), Boolean.TRUE.equals(r.infectionSuspected()), r.assessedBy()));
    audit.record("wound.assessed","wound_assessment",a.getId().toString(),"{}");
    var series = assessments.findByCaseIdOrderByAssessedAtAsc(id);
    double baseline = series.get(0).getAreaCm2()==null ? 0 : series.get(0).getAreaCm2();
    String verdict = WoundRules.healingVerdict(baseline, a.getAreaCm2()==null?0:a.getAreaCm2(), a.isInfectionSuspected());
    return Map.of("ok",true,"assessment",a,"area_cm2",a.getAreaCm2()==null?"":a.getAreaCm2(),
      "baseline_area_cm2",baseline,"verdict",verdict,
      "escalation_required", verdict.contains("escalate"));
  }

  @GetMapping("/cases/{id}/healing")
  Map<String,Object> healing(@PathVariable UUID id) {
    kase(id);
    var series = assessments.findByCaseIdOrderByAssessedAtAsc(id);
    if (series.isEmpty()) return Map.of("ok",true,"measurements",0,"verdict","no_baseline_yet");
    var first = series.get(0); var last = series.get(series.size()-1);
    double base = first.getAreaCm2()==null?0:first.getAreaCm2();
    double cur = last.getAreaCm2()==null?0:last.getAreaCm2();
    return Map.of("ok",true,"measurements",series.size(),"baseline_area_cm2",base,"current_area_cm2",cur,
      "change_percent", WoundRules.areaChangePercent(base, cur),
      "verdict", WoundRules.healingVerdict(base, cur, last.isInfectionSuspected()));
  }

  public record ImageRequest(UUID assessmentId, String consentBasis, String purpose, String bodySite, String photographer) {}
  @PostMapping("/cases/{id}/images")
  Map<String,Object> image(@PathVariable UUID id, @RequestBody ImageRequest r) {
    var p = principal(); var c = kase(id);
    // No consent basis, no capture — policy enforced at the door.
    if (!WoundRules.imageConsentValid(r.consentBasis()))
      throw BizException.badRequest("consent_basis_required","صورة سريرية بلا موافقة أو أساس صالح تُمنع (دورة 250)");
    var img = images.save(new ClinicalImage(c.getFacilityId(), p.governorate(), id, r.assessmentId(),
      r.consentBasis(), r.purpose(), r.bodySite(), r.photographer()));
    audit.record("wound.image.captured","clinical_image",img.getId().toString(),"{}");
    return Map.of("ok",true,"image",img,"access_policy","restricted");
  }
  @GetMapping("/images/{id}")
  Map<String,Object> viewImage(@PathVariable UUID id) {
    var p = principal();
    if (!WoundRules.mayViewImage(p.role())) {
      audit.record("wound.image.view_denied","clinical_image",id.toString(),"{}");
      throw BizException.forbidden("الصور السريرية مقيدة العرض لأدوار محددة — سُجلت المحاولة");
    }
    var img = images.findById(id).orElseThrow(() -> BizException.notFound("image_not_found","الصورة غير موجودة"));
    audit.record("wound.image.viewed","clinical_image",id.toString(),"{}");
    return Map.of("ok",true,"image",img);
  }

  public record DressingRequest(String material, String materialLot, String lotStatus, Integer quantity, String appliedBy, String response) {}
  @PostMapping("/cases/{id}/dressings")
  Map<String,Object> dressing(@PathVariable UUID id, @RequestBody DressingRequest r) {
    var p = principal(); var c = kase(id);
    if (r.material()==null||r.materialLot()==null) throw BizException.badRequest("invalid_dressing","المادة وتشغيلتها مطلوبتان للتتبع");
    String lotStatus = r.lotStatus()==null ? "ok" : r.lotStatus();
    if (!WoundRules.materialUsable(lotStatus))
      throw BizException.conflict("material_blocked","مادة منتهية أو مستدعاة محظورة الاستخدام — تتبع التشغيلة (دورة 250)");
    var d = dressings.save(new DressingEpisode(c.getFacilityId(), p.governorate(), id, r.material(), r.materialLot(),
      lotStatus, r.quantity()==null?1:r.quantity(), r.appliedBy(), r.response()));
    audit.record("wound.dressing.applied","dressing_episode",d.getId().toString(),"{}");
    return Map.of("ok",true,"dressing",d);
  }

  public record ResuscitationRequest(Double weightKg, Double tbsaPercent) {}
  @PostMapping("/cases/{id}/resuscitation")
  Map<String,Object> resuscitate(@PathVariable UUID id, @RequestBody ResuscitationRequest r) {
    var p = principal(); var c = kase(id);
    Double tbsa = r.tbsaPercent()!=null ? r.tbsaPercent() : c.getTbsaPercent();
    if (r.weightKg()==null || !WoundRules.tbsaValid(tbsa))
      throw BizException.badRequest("resuscitation_inputs","إنعاش الحروق يتطلب الوزن وTBSA صحيحة (باركلاند)");
    var rec = resuscitation.save(new BurnResuscitationRecord(c.getFacilityId(), p.governorate(), id, r.weightKg(), tbsa));
    audit.record("wound.resuscitation.planned","burn_resuscitation_record",rec.getId().toString(),"{}");
    return Map.of("ok",true,"resuscitation",rec,"formula","Parkland 4ml x kg x %TBSA",
      "planned_ml_24h",rec.getPlannedMl24h(),"planned_ml_first_8h",rec.getPlannedMlFirst8h());
  }
  @PostMapping("/resuscitation/{id}/actuals")
  Map<String,Object> actuals(@PathVariable UUID id, @RequestBody Map<String,Double> body) {
    var rec = resuscitation.findById(id).orElseThrow(() -> BizException.notFound("record_not_found","سجل الإنعاش غير موجود"));
    rec.recordActuals(body.get("actualMl"), body.get("urineOutputMlHr")); resuscitation.save(rec);
    audit.record("wound.resuscitation.actuals","burn_resuscitation_record",id.toString(),"{}");
    return Map.of("ok",true,"resuscitation",rec);
  }

  @GetMapping("/cases")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = cases.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"cases",rows,"count",rows.size());
  }
}
