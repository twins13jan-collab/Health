package eg.gov.health.platform.mentalhealth;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Mental health & addiction (cycle 252) — STRICT confidentiality.
 *  Reads are limited to the treating team (need-to-know roles); every read
 *  and every denied attempt is audited. List views NEVER carry the
 *  diagnosis. Emergency summaries expose the minimum necessary and need a
 *  documented legitimate basis. */
@RestController @RequestMapping("/api/mental-health")
public class MentalHealthController {
  private final MentalHealthEpisodeRepository episodes; private final AssessmentInstrumentRepository instruments;
  private final MhRiskAssessmentRepository risks; private final MhCarePlanRepository carePlans;
  private final SubstanceUseEpisodeRepository substance; private final AuditService audit;
  public MentalHealthController(MentalHealthEpisodeRepository episodes, AssessmentInstrumentRepository instruments,
                                MhRiskAssessmentRepository risks, MhCarePlanRepository carePlans,
                                SubstanceUseEpisodeRepository substance, AuditService audit) {
    this.episodes=episodes; this.instruments=instruments; this.risks=risks; this.carePlans=carePlans;
    this.substance=substance; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  /** Treating-team gate: non-treating access is refused AND recorded. */
  private TenantContext.Principal treatingPrincipal(String what) {
    var p = principal();
    if (!MentalHealthRules.canAccess(p.role())) {
      audit.record("mentalhealth.access.denied","mental_health_episode",what,"{}");
      throw BizException.forbidden("سجلات الصحة النفسية مقصورة على فريق العلاج — سُجلت المحاولة (دورة 252)");
    }
    return p;
  }
  private MentalHealthEpisode episode(UUID id) {
    return episodes.findById(id).orElseThrow(() -> BizException.notFound("episode_not_found","حلقة الصحة النفسية غير موجودة"));
  }
  /** Public-safe projection: no diagnosis, ever. */
  private Map<String,Object> safeView(MentalHealthEpisode e) {
    return Map.of("id",e.getId(),"patient_id",e.getPatientId(),"referral_reason",e.getReferralReason(),
      "urgency",e.getUrgency(),"status",e.getStatus());
  }

  public record EpisodeRequest(UUID patientId, String referralReason, String urgency, String provider, String diagnosisCode) {}
  @PostMapping("/episodes")
  Map<String,Object> open(@RequestBody EpisodeRequest r) {
    var p = treatingPrincipal("new");
    if (r.patientId()==null||r.referralReason()==null) throw BizException.badRequest("invalid_episode","المريض وسبب الإحالة مطلوبان");
    var e = episodes.save(new MentalHealthEpisode(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.referralReason(), r.urgency(), r.provider(), r.diagnosisCode()));
    audit.record("mentalhealth.episode.opened","mental_health_episode",e.getId().toString(),"{}");
    return Map.of("ok",true,"episode",safeView(e));
  }

  @GetMapping("/episodes")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    treatingPrincipal("list");
    var rows = episodes.findByPatientIdOrderByCreatedAtDesc(patientId);
    audit.record("mentalhealth.episodes.read","mental_health_episode",patientId.toString(),"{}");
    return Map.of("ok",true,"episodes",rows.stream().map(this::safeView).toList(),"count",rows.size(),
      "note","diagnosis withheld from list views by design");
  }

  @GetMapping("/episodes/{id}")
  Map<String,Object> detail(@PathVariable UUID id) {
    treatingPrincipal(id.toString());
    var e = episode(id);
    audit.record("mentalhealth.episode.read","mental_health_episode",id.toString(),"{}");
    return Map.of("ok",true,"episode",e); // full record — treating roles only, read audited
  }

  public record InstrumentRequest(String instrument, String instrumentVersion, Integer totalScore, Boolean item9Positive, String assessor) {}
  @PostMapping("/episodes/{id}/instruments")
  Map<String,Object> instrument(@PathVariable UUID id, @RequestBody InstrumentRequest r) {
    var p = treatingPrincipal(id.toString()); var e = episode(id);
    if (r.instrument()==null||r.instrumentVersion()==null||r.totalScore()==null)
      throw BizException.badRequest("invalid_instrument","الأداة ونسختها والدرجة مطلوبة");
    AssessmentInstrument a;
    try {
      a = instruments.save(new AssessmentInstrument(e.getFacilityId(), p.governorate(), id, r.instrument(),
        r.instrumentVersion(), r.totalScore(), Boolean.TRUE.equals(r.item9Positive()), r.assessor()));
    } catch (IllegalArgumentException ex) { throw BizException.badRequest("score_out_of_range", ex.getMessage()); }
    audit.record("mentalhealth.instrument.recorded","assessment_instrument",a.getId().toString(),"{}");
    return Map.of("ok",true,"instrument",a,"severity",a.getSeverity(),
      "suicide_risk_flag", a.isItem9Positive(),
      "flag_action", a.isItem9Positive() ? "PHQ-9 item 9 positive — risk assessment required regardless of total score" : "");
  }

  public record RiskRequest(String riskLevel, String protectiveFactors, String safetyPlan, String assessedBy) {}
  @PostMapping("/episodes/{id}/risk")
  Map<String,Object> risk(@PathVariable UUID id, @RequestBody RiskRequest r) {
    var p = treatingPrincipal(id.toString()); var e = episode(id);
    if (r.riskLevel()==null||!(r.riskLevel().equals("low")||r.riskLevel().equals("moderate")||r.riskLevel().equals("high")||r.riskLevel().equals("imminent")))
      throw BizException.badRequest("invalid_risk","مستوى خطر غير صحيح");
    if (MentalHealthRules.safetyPlanRequired(r.riskLevel()) && (r.safetyPlan()==null||r.safetyPlan().isBlank()))
      throw BizException.badRequest("safety_plan_required","خطة السلامة إلزامية عند خطر مرتفع أو وشيك (دورة 252)");
    var a = risks.save(new MhRiskAssessment(e.getFacilityId(), p.governorate(), id, r.riskLevel(), r.protectiveFactors(), r.safetyPlan(), r.assessedBy()));
    audit.record("mentalhealth.risk.assessed","mh_risk_assessment",a.getId().toString(),"{}");
    return Map.of("ok",true,"risk",a,"safety_protocol_activated",a.isSafetyProtocolActivated());
  }

  public record CarePlanRequest(String goals, String interventions, String medicationsSummary, LocalDate reviewDate) {}
  @PostMapping("/episodes/{id}/care-plan")
  Map<String,Object> carePlan(@PathVariable UUID id, @RequestBody CarePlanRequest r) {
    var p = treatingPrincipal(id.toString()); var e = episode(id);
    var cp = carePlans.save(new MhCarePlan(e.getFacilityId(), p.governorate(), id, r.goals(), r.interventions(), r.medicationsSummary(), r.reviewDate()));
    if ("open".equals(e.getStatus())) { e.setStatus("in_treatment"); episodes.save(e); }
    audit.record("mentalhealth.careplan.created","mh_care_plan",cp.getId().toString(),"{}");
    return Map.of("ok",true,"care_plan",cp);
  }

  public record SubstanceRequest(UUID patientId, UUID episodeId, String substance, String usePattern, Integer ciwaScore, String intervention) {}
  @PostMapping("/substance-use")
  Map<String,Object> substance(@RequestBody SubstanceRequest r) {
    var p = treatingPrincipal("substance");
    if (r.patientId()==null||r.substance()==null) throw BizException.badRequest("invalid_substance_episode","المريض والمادة مطلوبان");
    SubstanceUseEpisode s;
    try {
      s = substance.save(new SubstanceUseEpisode(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
        r.episodeId(), r.substance(), r.usePattern(), r.ciwaScore(), r.intervention()));
    } catch (IllegalArgumentException ex) { throw BizException.badRequest("ciwa_out_of_range", ex.getMessage()); }
    audit.record("mentalhealth.substance.recorded","substance_use_episode",s.getId().toString(),"{}");
    return Map.of("ok",true,"substance_episode",s,"withdrawal_band",s.getWithdrawalBand()==null?"":s.getWithdrawalBand(),
      "medical_monitoring_required", r.ciwaScore()!=null && MentalHealthRules.ciwaNeedsMedicalMonitoring(r.ciwaScore()));
  }

  /** Minimum-necessary emergency summary: risk + safety-plan existence +
   *  status only. No diagnosis, no notes. Requires a documented basis. */
  @GetMapping("/episodes/{id}/emergency-summary")
  Map<String,Object> emergencySummary(@PathVariable UUID id, @RequestParam(required=false) String basis) {
    treatingPrincipal(id.toString());
    if (basis==null||basis.isBlank())
      throw BizException.badRequest("basis_required","ملخص الطوارئ يتطلب أساسًا مشروعًا موثقًا (دورة 252)");
    var e = episode(id);
    var latestRisk = risks.findByEpisodeIdOrderByAssessedAtDesc(id).stream().findFirst().orElse(null);
    audit.record("mentalhealth.emergency_summary.read","mental_health_episode",id.toString(),"{}");
    return Map.of("ok",true,"patient_id",e.getPatientId(),"status",e.getStatus(),"urgency",e.getUrgency(),
      "latest_risk_level", latestRisk==null?"":latestRisk.getRiskLevel(),
      "safety_plan_exists", latestRisk!=null && latestRisk.hasSafetyPlan(),
      "basis", basis, "note","minimum necessary summary — diagnosis and notes withheld");
  }
}
