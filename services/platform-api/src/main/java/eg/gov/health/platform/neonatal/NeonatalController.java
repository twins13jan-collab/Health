package eg.gov.health.platform.neonatal;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Newborn, early screening & NICU (cycle 245, module 48). Identity is
 *  linked to the mother by identifiers — never by name alone. Invalid
 *  samples force recollection; abnormal results open a referral that
 *  closes only with an outcome; multiples require double verification;
 *  NICU discharge/transfer requires documented clinical handover. */
@RestController @RequestMapping("/api/neonatal")
public class NeonatalController {
  private final NewbornRecordRepository newborns; private final NeonatalScreeningRepository screenings;
  private final NicuStayRepository nicu; private final AuditService audit;
  public NeonatalController(NewbornRecordRepository newborns, NeonatalScreeningRepository screenings,
                            NicuStayRepository nicu, AuditService audit) {
    this.newborns=newborns; this.screenings=screenings; this.nicu=nicu; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private NewbornRecord newborn(UUID id) {
    return newborns.findById(id).orElseThrow(() -> BizException.notFound("newborn_not_found","سجل المولود غير موجود"));
  }
  private int siblingCount(NewbornRecord n) {
    if (n.getDeliveryId()!=null) return newborns.findByDeliveryId(n.getDeliveryId()).size();
    return 1;
  }

  public record NewbornRequest(UUID motherPatientId, UUID pregnancyId, UUID deliveryId, Integer birthOrder,
                               Boolean temporaryIdentifier, String sex, String birthPlace, Integer birthWeightG,
                               Double lengthCm, Double headCircumferenceCm, Integer gestationalWeeks,
                               Integer apgar1min, Integer apgar5min) {}
  @PostMapping("/newborns")
  Map<String,Object> register(@RequestBody NewbornRequest r) {
    var p = principal();
    if (r.motherPatientId()==null) throw BizException.badRequest("mother_required","ربط المولود بالأم بمعرفها مطلوب — لا اعتماد على الاسم (دورة 245)");
    if (r.apgar1min()!=null) NeonatalRules.apgarBand(r.apgar1min());
    if (r.apgar5min()!=null) NeonatalRules.apgarBand(r.apgar5min());
    var n = newborns.save(new NewbornRecord(UUID.fromString(p.facilityId()), p.governorate(), r.motherPatientId(),
      r.pregnancyId(), r.deliveryId(), r.birthOrder()==null?1:r.birthOrder(), Boolean.TRUE.equals(r.temporaryIdentifier()),
      r.sex(), r.birthPlace(), r.birthWeightG(), r.lengthCm(), r.headCircumferenceCm(), r.gestationalWeeks(),
      r.apgar1min(), r.apgar5min()));
    int siblings = siblingCount(n);
    audit.record("neonatal.newborn.registered","newborn_record",n.getId().toString(),"{}");
    return Map.of("ok",true,"newborn",n,
      "birth_weight_class", r.birthWeightG()==null?"":NeonatalRules.birthWeightClass(r.birthWeightG()),
      "apgar_5min_band", r.apgar5min()==null?"":NeonatalRules.apgarBand(r.apgar5min()),
      "apgar_escalation", r.apgar5min()!=null && NeonatalRules.apgarNeedsEscalation(r.apgar5min()),
      "siblings", siblings, "double_verification_required", NeonatalRules.doubleVerificationRequired(siblings));
  }

  public record ScreeningRequest(String screeningType, OffsetDateTime sampleAt) {}
  @PostMapping("/newborns/{id}/screenings")
  Map<String,Object> orderScreening(@PathVariable UUID id, @RequestBody ScreeningRequest r) {
    var p = principal(); var n = newborn(id);
    if (r.screeningType()==null) throw BizException.badRequest("screening_type_required","نوع الفحص مطلوب");
    var s = screenings.save(new NeonatalScreening(n.getFacilityId(), p.governorate(), id, r.screeningType(),
      r.sampleAt()==null?OffsetDateTime.now():r.sampleAt()));
    audit.record("neonatal.screening.ordered","neonatal_screening",s.getId().toString(),"{}");
    return Map.of("ok",true,"screening",s);
  }

  public record ResultRequest(String result, String acknowledgedBy) {}
  @PostMapping("/screenings/{id}/result")
  Map<String,Object> result(@PathVariable UUID id, @RequestBody ResultRequest r) {
    var s = screenings.findById(id).orElseThrow(() -> BizException.notFound("screening_not_found","الفحص غير موجود"));
    if (!"pending".equals(s.getResult()) && !s.isRecollectRequired())
      throw BizException.conflict("result_final","نتيجة هذا الفحص نهائية — العينة غير الصالحة فقط تُعاد");
    NeonatalRules.ScreeningOutcome outcome;
    try { outcome = NeonatalRules.onResult(r.result()==null?"":r.result()); }
    catch (IllegalArgumentException ex) { throw BizException.badRequest("invalid_result", ex.getMessage()); }
    if (outcome.requiresAcknowledgment() && (r.acknowledgedBy()==null||r.acknowledgedBy().isBlank()))
      throw BizException.badRequest("ack_required","النتيجة غير الطبيعية تتطلب إقرار استلام من فريق المتابعة (دورة 245)");
    s.applyResult(outcome, r.acknowledgedBy()); screenings.save(s);
    audit.record("neonatal.screening.result."+outcome.result(),"neonatal_screening",id.toString(),"{}");
    return Map.of("ok",true,"screening",s,"recollect_required",outcome.recollectRequired(),
      "referral_opened",outcome.referralOpens());
  }

  @PostMapping("/screenings/{id}/referral/close")
  Map<String,Object> closeReferral(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var s = screenings.findById(id).orElseThrow(() -> BizException.notFound("screening_not_found","الفحص غير موجود"));
    String outcome = body.get("outcome");
    if (!NeonatalRules.referralClosable(s.getReferralStatus(), outcome))
      throw BizException.badRequest("outcome_required","إغلاق الإحالة يتطلب نتيجة موثقة وإحالة مفتوحة");
    s.closeReferral(outcome); screenings.save(s);
    audit.record("neonatal.screening.referral.closed","neonatal_screening",id.toString(),"{}");
    return Map.of("ok",true,"screening",s);
  }

  public record NicuRequest(String reason, String respiratorySupport, String feedingType, String handoverNotes) {}
  @PostMapping("/newborns/{id}/nicu")
  Map<String,Object> admitNicu(@PathVariable UUID id, @RequestBody NicuRequest r) {
    var p = principal(); var n = newborn(id);
    if (r.reason()==null) throw BizException.badRequest("reason_required","سبب دخول الحضانة مطلوب");
    var s = nicu.save(new NicuStay(n.getFacilityId(), p.governorate(), id, r.reason(), r.respiratorySupport(), r.feedingType(), r.handoverNotes()));
    n.setStatus("nicu"); newborns.save(n);
    audit.record("neonatal.nicu.admitted","nicu_stay",s.getId().toString(),"{}");
    return Map.of("ok",true,"nicu_stay",s,"documented_handover_to_nicu", r.handoverNotes()!=null && !r.handoverNotes().isBlank());
  }
  @PostMapping("/nicu/{id}/end")
  Map<String,Object> endNicu(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var s = nicu.findById(id).orElseThrow(() -> BizException.notFound("nicu_not_found","إقامة الحضانة غير موجودة"));
    if (!"active".equals(s.getStatus())) throw BizException.conflict("already_ended","الإقامة منتهية مسبقًا");
    String to = body.getOrDefault("status","discharged");
    if (!(to.equals("discharged")||to.equals("transferred")||to.equals("deceased")))
      throw BizException.badRequest("invalid_end_status","حالة إنهاء غير صحيحة");
    String handover = body.get("handoverNotes");
    // Transfer/discharge of a newborn requires a documented clinical handover.
    if ((to.equals("transferred")||to.equals("discharged")) && (handover==null||handover.isBlank()))
      throw BizException.badRequest("handover_required","نقل/خروج المولود يتطلب تسليمًا سريريًا موثقًا (دورة 245)");
    s.end(to, handover); nicu.save(s);
    newborns.findById(s.getNewbornId()).ifPresent(n -> { n.setStatus(to); newborns.save(n); });
    audit.record("neonatal.nicu."+to,"nicu_stay",id.toString(),"{}");
    return Map.of("ok",true,"nicu_stay",s);
  }

  /** NICU operational board (Phase 4.6): every active incubator stay of
   *  the caller's facility with the newborn's birth data classified by
   *  the cycle-245 rules and the screening pipeline state (pending,
   *  abnormal, open referrals, samples awaiting recollection). */
  @GetMapping("/board")
  Map<String,Object> board() {
    var p = principal();
    var active = nicu.findByFacilityIdAndStatus(UUID.fromString(p.facilityId()), "active");
    var rows = new ArrayList<Map<String,Object>>();
    int lowBirthWeight = 0, openReferrals = 0, pendingScreenings = 0, abnormal = 0;
    for (var s : active) {
      var n = newborns.findById(s.getNewbornId()).orElse(null);
      var scr = screenings.findByNewbornIdOrderByCreatedAtDesc(s.getNewbornId());
      long pend = scr.stream().filter(x -> "pending".equals(x.getResult())).count();
      long abn = scr.stream().filter(x -> "abnormal".equals(x.getResult())).count();
      long open = scr.stream().filter(x -> "open".equals(x.getReferralStatus())).count();
      long recol = scr.stream().filter(x -> x.isRecollectRequired()).count();
      pendingScreenings += (int) pend; abnormal += (int) abn; openReferrals += (int) open;
      Integer bw = n == null ? null : n.getBirthWeightG();
      if (bw != null && NeonatalRules.isLowBirthWeight(bw)) lowBirthWeight++;
      var row = new LinkedHashMap<String,Object>();
      row.put("stay", s);
      row.put("birth_weight_g", bw == null ? "" : bw);
      row.put("weight_class", bw == null ? "" : NeonatalRules.birthWeightClass(bw));
      row.put("apgar_5min", n == null || n.getApgar5min() == null ? "" : n.getApgar5min());
      row.put("apgar_band", n == null || n.getApgar5min() == null ? "" : NeonatalRules.apgarBand(n.getApgar5min()));
      row.put("screenings_total", scr.size());
      row.put("screenings_pending", pend);
      row.put("screenings_abnormal", abn);
      row.put("referrals_open", open);
      row.put("recollect_required", recol);
      rows.add(row);
    }
    var summary = new LinkedHashMap<String,Object>();
    summary.put("active_stays", active.size()); summary.put("low_birth_weight", lowBirthWeight);
    summary.put("open_referrals", openReferrals); summary.put("pending_screenings", pendingScreenings);
    summary.put("abnormal_screenings", abnormal);
    return Map.of("ok", true, "board", rows, "count", rows.size(), "summary", summary);
  }

  @GetMapping("/newborns")
  Map<String,Object> byMother(@RequestParam UUID motherPatientId) {
    var rows = newborns.findByMotherPatientIdOrderByBornAtDesc(motherPatientId);
    return Map.of("ok",true,"newborns",rows,"count",rows.size());
  }
  @GetMapping("/newborns/{id}/screenings")
  Map<String,Object> screeningsOf(@PathVariable UUID id) {
    newborn(id); var rows = screenings.findByNewbornIdOrderByCreatedAtDesc(id);
    return Map.of("ok",true,"screenings",rows,"count",rows.size());
  }
}
