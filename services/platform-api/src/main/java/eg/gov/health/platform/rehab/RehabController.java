package eg.gov.health.platform.rehab;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Rehabilitation & therapy (cycle 246). Sessions require a baseline
 *  assessment and an APPROVED plan; acute pain/deterioration stops a
 *  session and escalates; missed sessions need a documented reason;
 *  improvement is claimed only from measured outcomes against baseline —
 *  session counts alone prove nothing. */
@RestController @RequestMapping("/api/rehab")
public class RehabController {
  private final RehabReferralRepository referrals; private final FunctionalAssessmentRepository assessments;
  private final RehabPlanRepository plans; private final TherapySessionRepository sessions;
  private final OutcomeMeasureRepository outcomes; private final DischargeRecommendationRepository discharges;
  private final AuditService audit;
  public RehabController(RehabReferralRepository referrals, FunctionalAssessmentRepository assessments,
                         RehabPlanRepository plans, TherapySessionRepository sessions,
                         OutcomeMeasureRepository outcomes, DischargeRecommendationRepository discharges, AuditService audit) {
    this.referrals=referrals; this.assessments=assessments; this.plans=plans; this.sessions=sessions;
    this.outcomes=outcomes; this.discharges=discharges; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private RehabReferral referral(UUID id) {
    return referrals.findById(id).orElseThrow(() -> BizException.notFound("referral_not_found","إحالة التأهيل غير موجودة"));
  }
  private RehabPlan plan(UUID id) {
    return plans.findById(id).orElseThrow(() -> BizException.notFound("plan_not_found","خطة التأهيل غير موجودة"));
  }
  private boolean baselineExists(UUID referralId) {
    return assessments.findByReferralIdOrderByAssessedAtAsc(referralId).stream().anyMatch(a -> "baseline".equals(a.getKind()));
  }

  public record ReferralRequest(UUID patientId, String reason, String diagnosis, String restrictions, String referringPhysician) {}
  @PostMapping("/referrals")
  Map<String,Object> refer(@RequestBody ReferralRequest r) {
    var p = principal();
    if (r.patientId()==null||r.reason()==null) throw BizException.badRequest("invalid_referral","المريض وسبب الإحالة مطلوبان");
    var ref = referrals.save(new RehabReferral(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.reason(), r.diagnosis(), r.restrictions(), r.referringPhysician()));
    audit.record("rehab.referral.received","rehab_referral",ref.getId().toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }

  public record AssessmentRequest(String kind, Integer mobilityScore, Integer balanceScore, Integer painScore,
                                  Integer adlScore, Integer cognitionScore, Integer communicationScore, String assessedBy) {}
  @PostMapping("/referrals/{id}/assessments")
  Map<String,Object> assess(@PathVariable UUID id, @RequestBody AssessmentRequest r) {
    var p = principal(); var ref = referral(id);
    var a = assessments.save(new FunctionalAssessment(ref.getFacilityId(), p.governorate(), id,
      r.kind()==null?"baseline":r.kind(), r.mobilityScore(), r.balanceScore(), r.painScore(), r.adlScore(),
      r.cognitionScore(), r.communicationScore(), r.assessedBy()));
    if ("baseline".equals(a.getKind()) && "received".equals(ref.getStatus())) { ref.setStatus("assessed"); referrals.save(ref); }
    audit.record("rehab.assessment.completed","functional_assessment",a.getId().toString(),"{}");
    return Map.of("ok",true,"assessment",a);
  }

  public record PlanRequest(String goals, String baselineSummary, String responsible, Integer targetAdlScore) {}
  @PostMapping("/referrals/{id}/plan")
  Map<String,Object> plan(@PathVariable UUID id, @RequestBody PlanRequest r) {
    var p = principal(); var ref = referral(id);
    if (r.goals()==null||r.goals().isBlank()) throw BizException.badRequest("goals_required","أهداف قابلة للقياس مطلوبة لخطة التأهيل (دورة 246)");
    if (!baselineExists(id)) throw BizException.conflict("baseline_required","لا خطة دون تقييم أساسي موثق");
    var pl = plans.save(new RehabPlan(ref.getFacilityId(), p.governorate(), id, r.goals(), r.baselineSummary(), r.responsible(), r.targetAdlScore()));
    audit.record("rehab.plan.created","rehab_plan",pl.getId().toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }
  @PostMapping("/plans/{id}/approve")
  Map<String,Object> approve(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var pl = plan(id);
    String by = body.get("approvedBy");
    if (by==null||by.isBlank()) throw BizException.badRequest("approver_required","اعتماد اختصاصي التأهيل مطلوب");
    if (!"draft".equals(pl.getStatus())) throw BizException.conflict("not_draft","الخطة ليست مسودة");
    pl.approve(by); plans.save(pl);
    var ref = referral(pl.getReferralId()); ref.setStatus("plan_active"); referrals.save(ref);
    audit.record("rehab.plan.approved","rehab_plan",id.toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }

  public record SessionRequest(String sessionType, String intervention, Integer durationMin, Integer painDuring,
                               String response, String incident, String status, String missedReason, String therapist) {}
  @PostMapping("/plans/{id}/sessions")
  Map<String,Object> session(@PathVariable UUID id, @RequestBody SessionRequest r) {
    var p = principal(); var pl = plan(id);
    if (!RehabRules.sessionsAllowed(baselineExists(pl.getReferralId()), pl.getStatus()))
      throw BizException.conflict("plan_not_ready","الجلسات تتطلب تقييمًا أساسيًا وخطة معتمدة (دورة 246)");
    String status = r.status()==null ? "completed" : r.status();
    if ("missed".equals(status) && !RehabRules.missedReasonValid(r.missedReason()))
      throw BizException.badRequest("missed_reason_required","الجلسة الفائتة تتطلب سببًا موثقًا وخطة متابعة");
    boolean stop = RehabRules.mustStopAndEscalate(r.painDuring(), r.incident());
    if (stop) status = "stopped_escalated";
    var s = sessions.save(new TherapySession(pl.getFacilityId(), p.governorate(), id, r.sessionType(), r.intervention(),
      r.durationMin(), r.painDuring(), r.response(), r.incident(), status, r.missedReason(), r.therapist()));
    if ("approved".equals(pl.getStatus())) { pl.setStatus("active"); plans.save(pl); }
    audit.record("rehab.session."+status,"therapy_session",s.getId().toString(),"{}");
    return Map.of("ok",true,"session",s,"escalated",stop,
      "escalation_action", stop ? "intervention stopped — refer to treating team immediately" : "");
  }

  public record OutcomeRequest(String instrument, String instrumentVersion, Double score, String assessor) {}
  @PostMapping("/plans/{id}/outcomes")
  Map<String,Object> outcome(@PathVariable UUID id, @RequestBody OutcomeRequest r) {
    var p = principal(); var pl = plan(id);
    if (!RehabRules.outcomeValid(r.instrument(), r.instrumentVersion()) || r.score()==null)
      throw BizException.badRequest("invalid_outcome","قياس النتيجة يتطلب مقياسًا معروفًا ونسخة موثقة ودرجة (دورة 246)");
    var o = outcomes.save(new OutcomeMeasure(pl.getFacilityId(), p.governorate(), id, r.instrument(), r.instrumentVersion(), r.score(), r.assessor()));
    audit.record("rehab.outcome.measured","outcome_measure",o.getId().toString(),"{}");
    return Map.of("ok",true,"outcome",o);
  }

  @GetMapping("/plans/{id}/progress")
  Map<String,Object> progress(@PathVariable UUID id) {
    var pl = plan(id);
    var baseline = assessments.findByReferralIdOrderByAssessedAtAsc(pl.getReferralId()).stream()
      .filter(a -> "baseline".equals(a.getKind())).findFirst().orElse(null);
    var measures = outcomes.findByPlanIdOrderByMeasuredAtAsc(id);
    var sessionRows = sessions.findByPlanIdOrderBySessionAtAsc(id);
    Double base = baseline==null||baseline.getAdlScore()==null ? null : baseline.getAdlScore().doubleValue();
    // ADL outcome tracking uses the ADL instrument measures when present.
    var adlMeasures = measures.stream().filter(m -> "ADL".equals(m.getInstrument())).toList();
    Double latest = adlMeasures.isEmpty() ? null : adlMeasures.get(adlMeasures.size()-1).getScore();
    String verdict = RehabRules.progressVerdict(base, latest, pl.getTargetAdlScore(), sessionRows.size());
    return Map.of("ok",true,"plan_id",id,"baseline_adl",base==null?"":base,"latest_adl",latest==null?"":latest,
      "delta", (base!=null&&latest!=null) ? RehabRules.delta(base, latest) : "",
      "sessions_count", sessionRows.size(), "verdict", verdict,
      "note", "session count alone never proves improvement — verdict is measured-only");
  }

  public record DischargeRequest(String decision, String homePlan, String assistiveDevices, String followUp) {}
  @PostMapping("/plans/{id}/discharge")
  Map<String,Object> discharge(@PathVariable UUID id, @RequestBody DischargeRequest r) {
    var p = principal(); var pl = plan(id);
    if (r.decision()==null||!(r.decision().equals("discharge")||r.decision().equals("continue")||r.decision().equals("refer")))
      throw BizException.badRequest("invalid_decision","قرار الخطة: خروج/استمرار/إحالة");
    var d = discharges.save(new DischargeRecommendation(pl.getFacilityId(), p.governorate(), id, r.decision(), r.homePlan(), r.assistiveDevices(), r.followUp()));
    if (!"continue".equals(r.decision())) {
      pl.setStatus("closed"); plans.save(pl);
      var ref = referral(pl.getReferralId());
      ref.setStatus("discharge".equals(r.decision()) ? "discharged" : "referred"); referrals.save(ref);
    }
    audit.record("rehab.plan.closed."+r.decision(),"discharge_recommendation",d.getId().toString(),"{}");
    return Map.of("ok",true,"recommendation",d);
  }

  @GetMapping("/referrals")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = referrals.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"referrals",rows,"count",rows.size());
  }
}
