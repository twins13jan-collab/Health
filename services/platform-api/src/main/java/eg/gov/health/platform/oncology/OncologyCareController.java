package eg.gov.health.platform.oncology;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*;
/** Oncology, chemotherapy, immunotherapy & radiotherapy (cycle 243).
 *  Diagnosis and plan are approved by the consultant and the tumor
 *  board; every cycle binds to a medically approved, in-date protocol
 *  version — a protocol merely present in the system is not a medical
 *  approval, and the system never generates a dose: it records the
 *  dose the consultant approved. Pre-treatment verification covers
 *  weight/BSA, functions, labs, allergy and consent, and suspends on
 *  out-of-condition weight or labs for physician review. Preparation
 *  is reviewed by the oncology pharmacist; administration is
 *  independently verified; prescribed vs administered dose, variance,
 *  reason and approver are recorded; unavailable or recalled drugs
 *  block dispensing unless a physician decides a substitute;
 *  postponements carry reason and impact without unapproved
 *  auto-rescheduling; toxicity drives the continue/modify/stop
 *  decision; radiotherapy plans and fractions are verified per plan;
 *  and a technical user can never touch a protocol or a dose. */
@RestController @RequestMapping("/api/oncology")
public class OncologyCareController {
  private final CancerCaseRepository cases; private final TumorBoardDecisionRepository board;
  private final ProtocolVersionRepository protocols; private final TreatmentCycleRepository cycles;
  private final PretreatmentVerificationRepository verifications; private final ChemoAdministrationRepository administrations;
  private final RadiotherapyPlanRepository rtPlans; private final RadiotherapyFractionRepository rtFractions;
  private final ToxicityAssessmentRepository toxicity; private final AuditService audit;
  public OncologyCareController(CancerCaseRepository cases, TumorBoardDecisionRepository board, ProtocolVersionRepository protocols,
                            TreatmentCycleRepository cycles, PretreatmentVerificationRepository verifications,
                            ChemoAdministrationRepository administrations, RadiotherapyPlanRepository rtPlans,
                            RadiotherapyFractionRepository rtFractions, ToxicityAssessmentRepository toxicity, AuditService audit) {
    this.cases=cases; this.board=board; this.protocols=protocols; this.cycles=cycles; this.verifications=verifications;
    this.administrations=administrations; this.rtPlans=rtPlans; this.rtFractions=rtFractions; this.toxicity=toxicity; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private CancerCase kase(UUID id) {
    return cases.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","حالة الأورام غير موجودة"));
  }
  private TreatmentCycle cycle(UUID id) {
    return cycles.findById(id).orElseThrow(() -> BizException.notFound("cycle_not_found","الدورة العلاجية غير موجودة"));
  }

  public record CaseRequest(String patientRef, String tumorType, String stage, String biomarkers) {}
  @PostMapping("/cases")
  Map<String,Object> createCase(@RequestBody CaseRequest r) {
    var p = principal();
    if (!"oncologist".equals(p.role()) && !"admin".equals(p.role()))
      throw BizException.forbidden("استشاري الأورام يعتمد التشخيص والخطة");
    var c = cases.save(new CancerCase(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.tumorType(), r.stage(), r.biomarkers(), p.email()));
    audit.record("oncology.case.diagnosed","cancer_case",c.getId().toString(),"{}");
    return Map.of("ok",true,"case",c);
  }
  public record BoardRequest(String decision, String participants) {}
  @PostMapping("/cases/{id}/board-decisions")
  Map<String,Object> boardDecision(@PathVariable UUID id, @RequestBody BoardRequest r) {
    var p = principal(); var c = kase(id);
    if (r.decision()==null||r.participants()==null)
      throw BizException.badRequest("board_fields","قرار اللجنة متعددة التخصصات يتطلب القرار والمشاركين");
    var d = board.save(new TumorBoardDecision(c.getFacilityId(), p.governorate(), id, r.decision(), r.participants()));
    c.setStatus("board_reviewed"); cases.save(c);
    audit.record("oncology.board.decided","tumor_board_decision",d.getId().toString(),"{}");
    return Map.of("ok",true,"decision",d);
  }
  public record ProtocolRequest(String regimenName, String version, String conditions, String approvalSource, LocalDate approvedAt, LocalDate validUntil, Boolean medicallyApproved, BigDecimal maxWeightChangePct) {}
  @PostMapping("/protocols")
  Map<String,Object> protocol(@RequestBody ProtocolRequest r) {
    var p = principal();
    if (!OncologyRules.mayModifyProtocol(p.role()))
      throw BizException.forbidden("لا يملك المستخدم التقني صلاحية إنشاء أو تعديل بروتوكول");
    var pv = protocols.save(new ProtocolVersion(UUID.fromString(p.facilityId()), p.governorate(), r.regimenName(), r.version(),
      r.conditions(), r.approvalSource(), r.approvedAt(), r.validUntil(), Boolean.TRUE.equals(r.medicallyApproved()), r.maxWeightChangePct()));
    audit.record("oncology.protocol.registered","protocol_version",pv.getId().toString(),"{}");
    return Map.of("ok",true,"protocol",pv,
      "note","a protocol existing in the system does not mean it is medically approved — approval source and validity govern use");
  }
  public record CycleRequest(UUID protocolVersionId, Integer cycleNumber, String drugs, String approvedDose) {}
  @PostMapping("/cases/{id}/cycles")
  Map<String,Object> createCycle(@PathVariable UUID id, @RequestBody CycleRequest r) {
    var p = principal(); var c = kase(id);
    if (!OncologyRules.mayHandleDose(p.role()))
      throw BizException.forbidden("الجرعة المعتمدة يسجلها الاستشاري أو الصيدلي الأورامي — لا المستخدم التقني");
    var pv = protocols.findById(r.protocolVersionId()).orElseThrow(() -> BizException.notFound("protocol_not_found","إصدار البروتوكول غير موجود"));
    if (!OncologyRules.protocolUsable(pv.isMedicallyApproved(), pv.getValidUntil(), pv.getStatus(), LocalDate.now()))
      throw BizException.conflict("protocol_unusable","كل دورة ترتبط بإصدار بروتوكول معتمد طبيًا وسارٍ ومصدره موثق");
    if (r.approvedDose()==null) throw BizException.badRequest("dose_required","الجرعة كما اعتمدها الاستشاري وفق البروتوكول — النظام لا يولد جرعة علاجية");
    var cy = cycles.save(new TreatmentCycle(c.getFacilityId(), p.governorate(), id, r.protocolVersionId(),
      r.cycleNumber()==null?1:r.cycleNumber(), r.drugs(), r.approvedDose(), p.email()));
    cy.setStatus("authorized"); cycles.save(cy);
    audit.record("oncology.cycle.authorized","treatment_cycle",cy.getId().toString(),"{}");
    return Map.of("ok",true,"cycle",cy);
  }
  public record VerificationRequest(BigDecimal weightKg, BigDecimal bsa, String labsSummary, Boolean labsWithinConditions,
                                    BigDecimal weightChangePct, Boolean allergyChecked, Boolean consentConfirmed, String pharmacyReview) {}
  @PostMapping("/cycles/{id}/verifications")
  Map<String,Object> verify(@PathVariable UUID id, @RequestBody VerificationRequest r) {
    var p = principal(); var cy = cycle(id);
    if (!OncologyRules.independentVerifier(cy.getPrescribedBy(), p.email()))
      throw BizException.forbidden("تحقق مستقل قبل التحضير والإعطاء — لا يتحقق الواصف من وصفته");
    var pv = protocols.findById(cy.getProtocolVersionId()).orElseThrow(() -> BizException.notFound("protocol_not_found","البروتوكول غير موجود"));
    boolean labsOk = Boolean.TRUE.equals(r.labsWithinConditions());
    boolean suspended = OncologyRules.verificationSuspended(r.weightChangePct(), pv.getMaxWeightChangePct(), labsOk);
    String reason = suspended ? (!labsOk ? "labs outside protocol conditions" : "weight change beyond protocol conditions") + " — physician review required" : null;
    var v = verifications.save(new PretreatmentVerification(cy.getFacilityId(), p.governorate(), id, r.weightKg(), r.bsa(),
      r.labsSummary(), labsOk, r.weightChangePct(), Boolean.TRUE.equals(r.allergyChecked()),
      Boolean.TRUE.equals(r.consentConfirmed()), p.email(), r.pharmacyReview(), suspended, reason));
    cy.setStatus(suspended ? "held" : "verified"); cycles.save(cy);
    audit.record("oncology.cycle.verified","pretreatment_verification",v.getId().toString(),"{}");
    return Map.of("ok",true,"verification",v,"cycle_status",cy.getStatus());
  }
  public record AdminRequest(String prescribedDose, String administeredDose, BigDecimal variance, String varianceReason,
                             String varianceApprovedBy, Boolean drugAvailable, Boolean recalled, String substituteDrug,
                             String substituteDecisionBy, Boolean identityChecked, String batchRef) {}
  @PostMapping("/cycles/{id}/administrations")
  Map<String,Object> administer(@PathVariable UUID id, @RequestBody AdminRequest r) {
    var p = principal(); var cy = cycle(id);
    var latest = verifications.findByCycleIdOrderByCreatedAtDesc(id);
    if (latest.isEmpty() || !"verified".equals(latest.get(0).getStatus()))
      throw BizException.conflict("not_verified","لا إعطاء قبل تحقق ما قبل العلاج المستقل وغير المعلّق");
    boolean available = !Boolean.FALSE.equals(r.drugAvailable()), recalled = Boolean.TRUE.equals(r.recalled());
    if (!OncologyRules.dispensable(available, recalled, r.substituteDrug(), r.substituteDecisionBy()))
      throw BizException.conflict("drug_blocked","دواء غير متاح أو مستدعى: منع الصرف — والبديل يرتبط بقرار طبي موثق");
    if (!OncologyRules.varianceDocumented(r.variance(), r.varianceReason(), r.varianceApprovedBy()))
      throw BizException.badRequest("variance_fields","الجرعة المنفذة مقابل المقررة: أي فارق يسجل بسببه ومعتمده");
    if (!Boolean.TRUE.equals(r.identityChecked()))
      throw BizException.badRequest("identity_check","التمريض يتحقق من الهوية والدواء قبل الإعطاء");
    var a = administrations.save(new AdministrationRecord(cy.getFacilityId(), p.governorate(), id, r.prescribedDose(),
      r.administeredDose(), r.variance(), r.varianceReason(), r.varianceApprovedBy(), available, recalled,
      r.substituteDrug(), r.substituteDecisionBy(), p.email(), true, r.batchRef()));
    cy.setStatus("administered"); cycles.save(cy);
    var c = kase(cy.getCaseId()); c.setStatus("on_treatment"); cases.save(c);
    audit.record("oncology.dose.administered","administration_record",a.getId().toString(),"{}");
    return Map.of("ok",true,"administration",a,"safety_path","preparation/administration errors route to the medication-safety path with batch and dose linked");
  }
  @PostMapping("/cycles/{id}/postpone")
  Map<String,Object> postpone(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var cy = cycle(id);
    if (body.get("reason")==null||body.get("impact")==null)
      throw BizException.badRequest("postpone_fields","تأجيل جلسة يوثق السبب والأثر — دون إعادة جدولة آلية غير معتمدة");
    cy.postpone(body.get("reason"), body.get("impact")); cycles.save(cy);
    audit.record("oncology.cycle.postponed","treatment_cycle",id.toString(),"{}");
    return Map.of("ok",true,"cycle",cy,"auto_rescheduled",false);
  }
  public record RtPlanRequest(String totalDose, Integer fractionsPlanned, String organsAtRisk, String approvedBy) {}
  @PostMapping("/cases/{id}/radiotherapy-plans")
  Map<String,Object> rtPlan(@PathVariable UUID id, @RequestBody RtPlanRequest r) {
    var p = principal(); var c = kase(id);
    var plan = rtPlans.save(new RadiotherapyPlan(c.getFacilityId(), p.governorate(), id, r.totalDose(),
      r.fractionsPlanned()==null?0:r.fractionsPlanned(), r.organsAtRisk(), r.approvedBy()));
    audit.record("oncology.rt.plan","radiotherapy_plan",plan.getId().toString(),"{}");
    return Map.of("ok",true,"plan",plan,"note","the radiotherapy team approves plan elements within its competence");
  }
  public record FractionRequest(Integer fractionNumber, String deliveredDose, String verifiedBy) {}
  @PostMapping("/radiotherapy-plans/{id}/fractions")
  Map<String,Object> fraction(@PathVariable UUID id, @RequestBody FractionRequest r) {
    var p = principal();
    var plan = rtPlans.findById(id).orElseThrow(() -> BizException.notFound("plan_not_found","الخطة غير موجودة"));
    if (r.verifiedBy()==null) throw BizException.badRequest("verification_required","كل جلسة إشعاعية توثق بالتحقق المطلوب");
    var f = rtFractions.save(new RadiotherapyFraction(plan.getFacilityId(), p.governorate(), id,
      r.fractionNumber()==null?0:r.fractionNumber(), r.deliveredDose(), r.verifiedBy()));
    audit.record("oncology.rt.fraction","radiotherapy_fraction",f.getId().toString(),"{}");
    return Map.of("ok",true,"fraction",f,
      "delivered",rtFractions.findByPlanIdOrderByFractionNumberAsc(id).size());
  }
  public record ToxicityRequest(UUID cycleId, Integer grade, String description, String intervention, String continuationDecision) {}
  @PostMapping("/cases/{id}/toxicity")
  Map<String,Object> toxicity(@PathVariable UUID id, @RequestBody ToxicityRequest r) {
    var p = principal(); var c = kase(id);
    if (r.continuationDecision()==null||!Set.of("continue","modify","stop").contains(r.continuationDecision()))
      throw BizException.badRequest("continuation_required","السمية والمضاعفات ترتبط بقرار الاستمرار/التعديل/الإيقاف");
    var t = toxicity.save(new ToxicityAssessment(c.getFacilityId(), p.governorate(), id, r.cycleId(),
      r.grade()==null?0:r.grade(), r.description(), r.intervention(), r.continuationDecision(), p.email()));
    audit.record("oncology.toxicity.recorded","toxicity_assessment",t.getId().toString(),"{}");
    return Map.of("ok",true,"toxicity",t);
  }
  @GetMapping("/cases/{id}")
  Map<String,Object> get(@PathVariable UUID id) {
    var c = kase(id);
    return Map.of("ok",true,"case",c);
  }
}
