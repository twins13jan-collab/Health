package eg.gov.health.platform.insurancedeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Health insurance, coverage & claims (module 113). Coverage
 *  providers, benefits and exclusions are managed; eligibility
 *  and pre-authorizations verified; claims, coding, audit,
 *  rejection and appeals governed; the performed service
 *  matched against documentation and tariff; settlements,
 *  payments and recoveries run; fraud signals reviewed by
 *  humans; and medical necessity kept separate from the
 *  financial coverage decision. */
@RestController @RequestMapping("/api/insurance-deep")
public class InsuranceDeepController {
  private final InsuranceClaimRepository claims; private final ClaimAppealRepository appeals; private final AuditService audit;
  public InsuranceDeepController(InsuranceClaimRepository claims, ClaimAppealRepository appeals, AuditService audit) {
    this.claims=claims; this.appeals=appeals; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private InsuranceClaim claim(UUID id) {
    return claims.findById(id).orElseThrow(() -> BizException.notFound("claim_not_found","المطالبة غير موجودة"));
  }

  public record ClaimBody(String patientRef, String serviceRef, Boolean documentationComplete, Boolean tariffMatch,
                          Boolean medicalNecessity, Boolean fraudFlag) {}
  @PostMapping("/claims")
  Map<String,Object> create(@RequestBody ClaimBody r) {
    var p = principal();
    var c = claims.save(new InsuranceClaim(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.serviceRef(),
      Boolean.TRUE.equals(r.documentationComplete()), Boolean.TRUE.equals(r.tariffMatch()), Boolean.TRUE.equals(r.medicalNecessity()),
      Boolean.TRUE.equals(r.fraudFlag())));
    return Map.of("ok",true,"claim",c);
  }
  public record DecideBody(String decision) {}
  @PostMapping("/claims/{id}/decide")
  Map<String,Object> decide(@PathVariable UUID id, @RequestBody DecideBody r) {
    principal(); var c = claim(id);
    if (!InsuranceRules.adjudicable(c.isDocumentationComplete(), c.isTariffMatch()))
      throw BizException.conflict("not_adjudicable","مطالبة لا تُفصل قبل مطابقة الخدمة المنفذة مع التوثيق والتعريفة");
    c.decide(r.decision()); claims.save(c);
    return Map.of("ok",true,"claim",c,"medical_necessity_recorded",c.isMedicalNecessity(),
      "note","the coverage decision is recorded beside medical necessity — it never rewrites it");
  }
  @PostMapping("/claims/{id}/review-fraud")
  Map<String,Object> reviewFraud(@PathVariable UUID id) {
    principal(); var c = claim(id);
    c.reviewFraud(); claims.save(c);
    audit.record("insurance.fraud.reviewed","insurance_claim",id.toString(),"{}");
    return Map.of("ok",true,"claim",c);
  }
  @PostMapping("/claims/{id}/settle")
  Map<String,Object> settle(@PathVariable UUID id) {
    principal(); var c = claim(id);
    if (!InsuranceRules.settleable(c.isFraudFlag(), c.isFraudHumanReviewed()))
      throw BizException.conflict("fraud_review_pending","مطالبة موسومة بإشارة احتيال لا تُسوّى قبل مراجعة بشرية — الإشارة اشتباه لا حكم");
    c.settle(); claims.save(c);
    return Map.of("ok",true,"claim",c);
  }
  /** Claims operations board: every unsettled claim as an
   *  attention queue — unreviewed fraud flags first (nothing
   *  settles past them), open appeals, non-adjudicable claims
   *  with their gaps named, undecided, then decided awaiting
   *  settlement. */
  @GetMapping("/board")
  Map<String,Object> board() {
    var p = principal();
    UUID fid = UUID.fromString(p.facilityId());
    var allClaims = claims.findByFacilityId(fid);
    var allAppeals = appeals.findByFacilityId(fid);
    Set<UUID> openAppealClaims = new HashSet<>();
    for (var a : allAppeals) if (!a.isDecided()) openAppealClaims.add(a.getClaimId());
    var now = java.time.OffsetDateTime.now();
    List<Map<String,Object>> queue = new ArrayList<>();
    int fraudPending = 0, appealsOpen = 0, notAdj = 0, awaitingDec = 0, awaitingSettle = 0, settled = 0;
    for (var c : allClaims) {
      boolean adj = InsuranceRules.adjudicable(c.isDocumentationComplete(), c.isTariffMatch());
      boolean decided = c.getCoverageDecision() != null;
      String state = ClaimsBoardRules.claimState(c.isFraudFlag(), c.isFraudHumanReviewed(),
          openAppealClaims.contains(c.getId()), adj, decided, c.isSettled());
      switch (state) {
        case "fraud_review_pending" -> fraudPending++;
        case "appeal_open" -> appealsOpen++;
        case "not_adjudicable" -> notAdj++;
        case "awaiting_decision" -> awaitingDec++;
        case "awaiting_settlement" -> awaitingSettle++;
        case "settled" -> settled++;
        default -> { }
      }
      int rank = ClaimsBoardRules.claimRank(state);
      if (rank == 9) continue;
      var row = new LinkedHashMap<String,Object>();
      row.put("id", c.getId()); row.put("patient_ref", c.getPatientRef()); row.put("service_ref", c.getServiceRef());
      row.put("state", state); row.put("rank", rank);
      row.put("gaps", ClaimsBoardRules.gaps(c.isDocumentationComplete(), c.isTariffMatch()));
      row.put("adjudicable", adj);
      row.put("settleable", InsuranceRules.settleable(c.isFraudFlag(), c.isFraudHumanReviewed()));
      row.put("medical_necessity", c.isMedicalNecessity());
      row.put("coverage_decision", c.getCoverageDecision());
      row.put("fraud_flag", c.isFraudFlag()); row.put("fraud_reviewed", c.isFraudHumanReviewed());
      row.put("age_days", c.getCreatedAt() == null ? 0 : java.time.Duration.between(c.getCreatedAt(), now).toDays());
      queue.add(row);
    }
    queue.sort(Comparator.comparingInt((Map<String,Object> r) -> (Integer) r.get("rank"))
        .thenComparing(r -> -((Number) r.get("age_days")).longValue()));
    var summary = new LinkedHashMap<String,Object>();
    summary.put("claims_total", allClaims.size());
    summary.put("attention_claims", queue.size());
    summary.put("fraud_review_pending", fraudPending); summary.put("appeals_open", appealsOpen);
    summary.put("not_adjudicable", notAdj); summary.put("awaiting_decision", awaitingDec);
    summary.put("awaiting_settlement", awaitingSettle); summary.put("settled", settled);
    return Map.of("ok", true, "claims", queue, "summary", summary);
  }
  public record AppealBody(String grounds) {}
  @PostMapping("/claims/{id}/appeals")
  Map<String,Object> appeal(@PathVariable UUID id, @RequestBody AppealBody r) {
    var p = principal(); claim(id);
    var a = appeals.save(new ClaimAppeal(UUID.fromString(p.facilityId()), p.governorate(), id, r.grounds()));
    return Map.of("ok",true,"appeal",a);
  }
}
