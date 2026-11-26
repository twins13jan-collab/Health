package eg.gov.health.platform.maternity;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.patient.PatientRepository;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Maternity, pregnancy, delivery & postpartum (cycle 244, module 95).
 *  Risk scoring is advisory — only a physician confirms risk level.
 *  A signed-off delivery record is immutable. Postpartum discharge is
 *  blocked while danger signs persist. Mother and newborn records stay
 *  separate but linked (newborn registration lives in cycle 245). */
@RestController @RequestMapping("/api/maternity")
public class MaternityController {
  private final PregnancyEpisodeRepository pregnancies; private final AntenatalVisitRepository visits;
  private final LaborEpisodeRepository labors; private final DeliveryRecordRepository deliveries;
  private final PostpartumAssessmentRepository postpartum; private final AuditService audit;
  private final PatientRepository patients;
  public MaternityController(PregnancyEpisodeRepository pregnancies, AntenatalVisitRepository visits,
                             LaborEpisodeRepository labors, DeliveryRecordRepository deliveries,
                             PostpartumAssessmentRepository postpartum, AuditService audit,
                             PatientRepository patients) {
    this.pregnancies=pregnancies; this.visits=visits; this.labors=labors; this.deliveries=deliveries;
    this.postpartum=postpartum; this.audit=audit; this.patients=patients;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private PregnancyEpisode pregnancy(UUID id) {
    return pregnancies.findById(id).orElseThrow(() -> BizException.notFound("pregnancy_not_found","حلقة الحمل غير موجودة"));
  }

  public record PregnancyRequest(UUID patientId, LocalDate lmpDate, String datingSource, Integer fetusCount) {}
  @PostMapping("/pregnancies")
  Map<String,Object> register(@RequestBody PregnancyRequest r) {
    var p = principal();
    if (r.patientId()==null) throw BizException.badRequest("invalid_pregnancy","المريضة (الأم) مطلوبة");
    var e = pregnancies.save(new PregnancyEpisode(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.lmpDate(), r.datingSource(), r.fetusCount()==null?1:r.fetusCount()));
    audit.record("maternity.pregnancy.registered","pregnancy_episode",e.getId().toString(),"{}");
    Object ga = "";
    if (r.lmpDate()!=null) { var g = MaternityRules.gestationalAge(r.lmpDate(), LocalDate.now()); ga = Map.of("weeks",g.weeks(),"days",g.days()); }
    return Map.of("ok",true,"pregnancy",e,"edd",e.getEdd()==null?"":e.getEdd(),"gestational_age_now",ga);
  }

  public record VisitRequest(Integer bpSystolic, Integer bpDiastolic, Double weightKg, Double fundalHeightCm,
                             Integer fetalHeartRate, Boolean proteinuria, Boolean bleeding, Boolean diabetes,
                             Integer motherAge, Boolean previousCesarean, Boolean severeSymptoms,
                             String symptoms, String labsSummary, String education, String plan) {}
  @PostMapping("/pregnancies/{id}/visits")
  Map<String,Object> visit(@PathVariable UUID id, @RequestBody VisitRequest r) {
    var p = principal(); var e = pregnancy(id);
    if (!"ongoing".equals(e.getStatus())) throw BizException.conflict("pregnancy_not_ongoing","لا زيارات متابعة بعد انتهاء الحمل");
    Integer gaWeeks = null;
    if (e.getLmpDate()!=null) gaWeeks = MaternityRules.gestationalAge(e.getLmpDate(), LocalDate.now()).weeks();
    var v = visits.save(new AntenatalVisit(e.getFacilityId(), p.governorate(), id, gaWeeks, r.bpSystolic(), r.bpDiastolic(),
      r.weightKg(), r.fundalHeightCm(), r.fetalHeartRate(), Boolean.TRUE.equals(r.proteinuria()),
      r.symptoms(), r.labsSummary(), r.education(), r.plan()));
    // Advisory only — the obstetrician confirms any risk change.
    String suggestion = MaternityRules.riskSuggestion(r.bpSystolic(), r.bpDiastolic(), Boolean.TRUE.equals(r.proteinuria()),
      Boolean.TRUE.equals(r.bleeding()), Boolean.TRUE.equals(r.diabetes()), e.getFetusCount(), r.motherAge(),
      Boolean.TRUE.equals(r.previousCesarean()), Boolean.TRUE.equals(r.severeSymptoms()));
    audit.record("maternity.visit.recorded","antenatal_visit",v.getId().toString(),"{}");
    return Map.of("ok",true,"visit",v,"gestational_weeks",gaWeeks==null?"":gaWeeks,
      "risk_suggestion",suggestion,"risk_applied",e.getRiskLevel(),
      "escalation_required", suggestion.equals("critical"));
  }

  public record RiskRequest(String riskLevel, String riskFactors) {}
  @PostMapping("/pregnancies/{id}/risk")
  Map<String,Object> confirmRisk(@PathVariable UUID id, @RequestBody RiskRequest r) {
    pregnancy(id);
    if (r.riskLevel()==null || !(r.riskLevel().equals("low")||r.riskLevel().equals("high")||r.riskLevel().equals("critical")))
      throw BizException.badRequest("invalid_risk","مستوى خطورة غير صحيح");
    var e = pregnancy(id); e.confirmRisk(r.riskLevel(), r.riskFactors()); pregnancies.save(e);
    audit.record("maternity.risk.confirmed","pregnancy_episode",id.toString(),"{}");
    return Map.of("ok",true,"pregnancy",e);
  }

  public record LaborRequest(Double cervixDilationCm, String team) {}
  @PostMapping("/pregnancies/{id}/labor")
  Map<String,Object> startLabor(@PathVariable UUID id, @RequestBody LaborRequest r) {
    var p = principal(); var e = pregnancy(id);
    if (!"ongoing".equals(e.getStatus())) throw BizException.conflict("not_ongoing","الحمل ليس في حالة متابعة");
    var l = labors.save(new LaborEpisode(e.getFacilityId(), p.governorate(), id, r.cervixDilationCm(), r.team()));
    e.setStatus("in_labor"); pregnancies.save(e);
    audit.record("maternity.labor.started","labor_episode",l.getId().toString(),"{}");
    return Map.of("ok",true,"labor",l);
  }

  public record DeliveryRequest(UUID laborId, String deliveryType, Integer bloodLossMl, String complications, Integer newbornCount) {}
  @PostMapping("/pregnancies/{id}/delivery")
  Map<String,Object> deliver(@PathVariable UUID id, @RequestBody DeliveryRequest r) {
    var p = principal(); var e = pregnancy(id);
    if (!("in_labor".equals(e.getStatus())||"ongoing".equals(e.getStatus())))
      throw BizException.conflict("not_in_labor","لا ولادة دون مخاض/حمل جارٍ");
    if (r.deliveryType()==null||!(r.deliveryType().equals("vaginal")||r.deliveryType().equals("assisted")||r.deliveryType().equals("cesarean")))
      throw BizException.badRequest("invalid_delivery_type","نوع ولادة غير صحيح");
    int loss = r.bloodLossMl()==null?0:r.bloodLossMl();
    var d = deliveries.save(new DeliveryRecord(e.getFacilityId(), p.governorate(), id, r.laborId(), r.deliveryType(),
      loss, r.complications(), r.newbornCount()==null?e.getFetusCount():r.newbornCount()));
    if (r.laborId()!=null) labors.findById(r.laborId()).ifPresent(l -> { l.setStatus("delivered"); labors.save(l); });
    e.setStatus("delivered"); pregnancies.save(e);
    audit.record("maternity.delivery.completed","delivery_record",d.getId().toString(),"{}");
    return Map.of("ok",true,"delivery",d,"pph",d.isPph(),
      "emergency_path", d.isPph(), "next_step", d.isPph() ? "PPH emergency protocol + blood bank alert" : "postpartum monitoring");
  }

  @PostMapping("/deliveries/{id}/sign-off")
  Map<String,Object> signOff(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var d = deliveries.findById(id).orElseThrow(() -> BizException.notFound("delivery_not_found","سجل الولادة غير موجود"));
    if (d.isSignedOff()) throw BizException.conflict("already_signed","سجل الولادة معتمد مسبقًا — السجلات المعتمدة لا تُعدَّل (دورة 244)");
    String by = body.get("signedBy");
    if (by==null||by.isBlank()) throw BizException.badRequest("signer_required","اسم المعتمد مطلوب");
    d.signOff(by); deliveries.save(d);
    audit.record("maternity.delivery.signed_off","delivery_record",id.toString(),"{}");
    return Map.of("ok",true,"delivery",d);
  }

  public record PostpartumRequest(String bleedingStatus, Integer bpSystolic, Integer bpDiastolic, Integer painScore,
                                  String breastfeeding, String mentalState, String dangerSigns, String dischargeInstructions) {}
  @PostMapping("/pregnancies/{id}/postpartum")
  Map<String,Object> postpartum(@PathVariable UUID id, @RequestBody PostpartumRequest r) {
    var p = principal(); var e = pregnancy(id);
    boolean blocked = MaternityRules.dischargeBlocked(r.bleedingStatus(), r.bpSystolic(), r.bpDiastolic(), r.painScore(), r.mentalState());
    var a = postpartum.save(new PostpartumAssessment(e.getFacilityId(), p.governorate(), id, r.bleedingStatus(),
      r.bpSystolic(), r.bpDiastolic(), r.painScore(), r.breastfeeding(), r.mentalState(), r.dangerSigns(),
      !blocked, r.dischargeInstructions()));
    if (!blocked && "delivered".equals(e.getStatus())) { e.setStatus("postpartum"); pregnancies.save(e); }
    audit.record("maternity.postpartum.assessed","postpartum_assessment",a.getId().toString(),"{}");
    return Map.of("ok",true,"assessment",a,"discharge_blocked",blocked,
      "block_reason", blocked ? "danger signs present — physician review required before discharge" : "");
  }

  /** Labour-ward operational board (Phase 4.7): the facility's active
   *  maternity caseload as an attention queue — PPH emergencies first,
   *  then women in labour, critical risk, overdue pregnancies — with
   *  computed gestational age, the latest visit's blood pressure, and
   *  today's deliveries including unsigned records awaiting sign-off. */
  @GetMapping("/board")
  Map<String,Object> board() {
    var p = principal();
    var cairo = ZoneId.of("Africa/Cairo");
    var today = LocalDate.now(cairo);
    var dayStart = today.atStartOfDay(cairo).toOffsetDateTime();
    var all = pregnancies.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    var rows = new ArrayList<Map<String,Object>>();
    int inLabor = 0, highRisk = 0, overdue = 0, deliveriesToday = 0, pphToday = 0, unsigned = 0, dischargeBlockedCount = 0;
    for (var e : all) {
      var dels = deliveries.findByPregnancyIdOrderByDeliveredAtDesc(e.getId());
      boolean pphOpen = false;
      for (var d : dels) {
        if (!d.getDeliveredAt().isBefore(dayStart)) {
          deliveriesToday++;
          if (d.isPph()) pphToday++;
          if (!d.isSignedOff()) unsigned++;
        }
        if (d.isPph() && !d.isSignedOff()) pphOpen = true;
      }
      if ("delivered".equals(e.getStatus()) || "postpartum".equals(e.getStatus())) {
        // Ended pregnancies appear on the board only while a PPH record
        // still awaits sign-off or a postpartum discharge is blocked.
        var assess = postpartum.findByPregnancyIdOrderByAssessedAtDesc(e.getId());
        boolean blocked = !assess.isEmpty() && !assess.get(0).isDischargeReady();
        if (!pphOpen && !blocked) continue;
        if (blocked) dischargeBlockedCount++;
        var row = new LinkedHashMap<String,Object>();
        row.put("pregnancy", e);
        row.put("patient_name", patients.findById(e.getPatientId()).map(pt -> pt.getFullName()).orElse(""));
        row.put("attention_rank", MaternityBoardRules.attentionRank(pphOpen, false, e.getRiskLevel(), false));
        row.put("pph_open", pphOpen); row.put("discharge_blocked", blocked);
        row.put("in_labor", false); row.put("overdue", false);
        rows.add(row);
        continue;
      }
      var ga = e.getLmpDate() == null ? null : MaternityRules.gestationalAge(e.getLmpDate(), today);
      boolean od = MaternityBoardRules.isOverdue(e.getEdd(), today, e.getStatus());
      if ("in_labor".equals(e.getStatus())) inLabor++;
      if ("critical".equals(e.getRiskLevel()) || "high".equals(e.getRiskLevel())) highRisk++;
      if (od) overdue++;
      var latestVisit = visits.findByPregnancyIdOrderByVisitDateDesc(e.getId());
      var labs = labors.findByPregnancyIdOrderByStartedAtDesc(e.getId());
      var row = new LinkedHashMap<String,Object>();
      row.put("pregnancy", e);
      row.put("patient_name", patients.findById(e.getPatientId()).map(pt -> pt.getFullName()).orElse(""));
      row.put("ga_weeks", ga == null ? "" : ga.weeks());
      row.put("ga_days", ga == null ? "" : ga.days());
      row.put("overdue", od);
      row.put("in_labor", "in_labor".equals(e.getStatus()));
      row.put("labor_dilation_cm", labs.isEmpty() || !"in_labor".equals(labs.get(0).getStatus()) ? "" : labs.get(0).getCervixDilationCm());
      row.put("last_bp", latestVisit.isEmpty() || latestVisit.get(0).getBpSystolic() == null ? ""
        : latestVisit.get(0).getBpSystolic() + "/" + latestVisit.get(0).getBpDiastolic());
      row.put("severe_bp", !latestVisit.isEmpty() && MaternityRules.severePreeclampsia(
        latestVisit.get(0).getBpSystolic(), latestVisit.get(0).getBpDiastolic()));
      row.put("attention_rank", MaternityBoardRules.attentionRank(false, "in_labor".equals(e.getStatus()), e.getRiskLevel(), od));
      row.put("pph_open", false); row.put("discharge_blocked", false);
      rows.add(row);
    }
    rows.sort(Comparator.<Map<String,Object>>comparingInt(r -> (Integer) r.get("attention_rank")));
    var summary = new LinkedHashMap<String,Object>();
    summary.put("in_labor", inLabor); summary.put("high_risk", highRisk); summary.put("overdue", overdue);
    summary.put("deliveries_today", deliveriesToday); summary.put("pph_today", pphToday);
    summary.put("unsigned_deliveries", unsigned); summary.put("discharge_blocked", dischargeBlockedCount);
    summary.put("active_caseload", rows.size());
    return Map.of("ok", true, "board", rows, "count", rows.size(), "summary", summary);
  }

  @GetMapping("/pregnancies")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = pregnancies.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"pregnancies",rows,"count",rows.size());
  }
}
