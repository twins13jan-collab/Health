package eg.gov.health.platform.decisionregistry;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Traceable decision registry (module 163). Every significant
 *  decision is recorded with its competent maker and context;
 *  alternatives and the reasons for the choice are documented;
 *  the decision is linked to its approvals and protocol; review,
 *  amendment and revocation are managed as new records; and
 *  committee decisions carry their conflicts-of-interest
 *  position. */
@RestController @RequestMapping("/api/decision-registry")
public class DecisionRegistryController {
  private final TraceableDecisionRepository decisions; private final DecisionAmendmentRepository amendments; private final AuditService audit;
  public DecisionRegistryController(TraceableDecisionRepository decisions, DecisionAmendmentRepository amendments, AuditService audit) {
    this.decisions=decisions; this.amendments=amendments; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record DecisionBody(String decisionRef, String title, String decisionMaker, String authorityBasis, String contextSummary,
                             String alternativesConsidered, String rationale, String linkedApprovalRef, String linkedProtocolRef,
                             Boolean committeeDecision, Boolean coiClear, UUID supersedesId) {}
  @PostMapping("/decisions")
  Map<String,Object> register(@RequestBody DecisionBody r) {
    var p = principal();
    boolean committee = Boolean.TRUE.equals(r.committeeDecision());
    if (!DecisionRegistryRules.registerable(r.decisionMaker(), r.authorityBasis(), r.contextSummary(), r.alternativesConsidered(), r.rationale(), committee, Boolean.TRUE.equals(r.coiClear())))
      throw BizException.badRequest("decision_incomplete","قرار قابل للتتبع بصاحب اختصاص وسند وسياق وبدائل مدروسة وأسباب اختيار — وقرارات اللجان بموقف تعارض مصالح واضح");
    var d = decisions.save(new TraceableDecision(UUID.fromString(p.facilityId()), p.governorate(), r.decisionRef(), r.title(),
      r.decisionMaker(), r.authorityBasis(), r.contextSummary(), r.alternativesConsidered(), r.rationale(),
      r.linkedApprovalRef(), r.linkedProtocolRef(), committee, Boolean.TRUE.equals(r.coiClear()), r.supersedesId()));
    if (r.supersedesId() != null) {
      decisions.findById(r.supersedesId()).ifPresent(old -> { old.setStatus("superseded"); decisions.save(old); });
    }
    audit.record("decision.registered","traceable_decision",d.getId().toString(),"{}");
    return Map.of("ok",true,"decision_entry",d);
  }
  public record AmendBody(String action, String reason, String actor) {}
  @PostMapping("/decisions/{id}/amend")
  Map<String,Object> amend(@PathVariable UUID id, @RequestBody AmendBody r) {
    var p = principal();
    var d = decisions.findById(id).orElseThrow(() -> BizException.notFound("decision_not_found","القرار غير موجود"));
    if (!DecisionRegistryRules.amendmentValid(r.reason()))
      throw BizException.badRequest("reason_required","مراجعة أو تعديل أو إلغاء قرار بلا سبب موثق لا يحدث — التاريخ لا يُحرَّر");
    var a = amendments.save(new DecisionAmendment(UUID.fromString(p.facilityId()), p.governorate(), id, r.action(), r.reason(), r.actor()));
    if ("revoke".equals(r.action())) { d.setStatus("revoked"); decisions.save(d); }
    audit.record("decision."+r.action(),"decision_amendment",a.getId().toString(),"{}");
    return Map.of("ok",true,"amendment",a,"decision_status",d.getStatus());
  }
}
