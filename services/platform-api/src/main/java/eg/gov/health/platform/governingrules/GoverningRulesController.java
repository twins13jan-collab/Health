package eg.gov.health.platform.governingrules;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** General rules governing ALL modules (module 82). Multi-
 *  facility and governorate isolation applies to every table,
 *  file, message, task and report without exception; every
 *  sensitive record carries its data source, creation and
 *  modification times, owning body and audit trail; every
 *  external integration holds an authenticated identity, limited
 *  permissions, a kill mechanism and review; every protocol and
 *  clinical or medication rule is versioned, sourced, accredited
 *  and effective-dated; every AI output is advisory unless duly
 *  accredited, the final professional decision remaining with
 *  the specialist; every linked financial, inventory or
 *  medication transaction supports idempotency, reconciliation
 *  and audit; every indicator and availability state declares
 *  its source, freshness, confidence and limits of meaning;
 *  every data sharing is bound by purpose, legal basis,
 *  permission and the minimum necessary data; and no completion,
 *  production readiness or live national connection is declared
 *  without implementation, test and independent accreditation
 *  evidence. This registry enforces that last rule on all the
 *  others. */
@RestController @RequestMapping("/api/governing-rules")
public class GoverningRulesController {
  private final GoverningRuleRepository rules; private final RuleComplianceCheckRepository checks; private final AuditService audit;
  public GoverningRulesController(GoverningRuleRepository rules, RuleComplianceCheckRepository checks, AuditService audit) {
    this.rules=rules; this.checks=checks; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private GoverningRule rule(UUID id) {
    return rules.findById(id).orElseThrow(() -> BizException.notFound("rule_not_found","القاعدة غير موجودة"));
  }

  public record RuleBody(String ruleCode, String title, String description) {}
  @PostMapping("/rules")
  Map<String,Object> create(@RequestBody RuleBody r) {
    var p = principal();
    if (!rules.findByRuleCode(r.ruleCode()).isEmpty())
      throw BizException.conflict("duplicate_rule","قاعدة حاكمة واحدة لكل رمز — الدستور لا يتفرع");
    var rule = rules.save(new GoverningRule(UUID.fromString(p.facilityId()), p.governorate(), r.ruleCode(), r.title(), r.description()));
    return Map.of("ok",true,"rule",rule);
  }
  public record CheckBody(String scopeRef, Boolean compliant, String evidenceRef, String checkedBy, Boolean independent) {}
  @PostMapping("/rules/{id}/checks")
  Map<String,Object> check(@PathVariable UUID id, @RequestBody CheckBody r) {
    var p = principal(); rule(id);
    var c = checks.save(new RuleComplianceCheck(UUID.fromString(p.facilityId()), p.governorate(), id, r.scopeRef(),
      Boolean.TRUE.equals(r.compliant()), r.evidenceRef(), r.checkedBy(), Boolean.TRUE.equals(r.independent())));
    String verdict = GoverningRules.verdict(c.isCompliant(), c.getEvidenceRef(), c.isIndependent());
    return Map.of("ok",true,"check",c,"verdict",verdict,
      "note","compliance is verified only with evidence AND independence — self-declared is an assertion, not a proof");
  }
  @GetMapping("/scopes/{scope}/readiness")
  Map<String,Object> readiness(@PathVariable String scope) {
    principal();
    var all = rules.findAll();
    var scopeChecks = checks.findByScopeRef(scope);
    Set<UUID> verified = new HashSet<>();
    for (var c : scopeChecks)
      if ("verified".equals(GoverningRules.verdict(c.isCompliant(), c.getEvidenceRef(), c.isIndependent()))) verified.add(c.getRuleId());
    boolean ready = GoverningRules.scopeReady(all.size(), verified.size());
    return Map.of("ok",true,"scope_ref",scope,"rules_total",all.size(),"rules_verified",verified.size(),"ready",ready,
      "rule","no readiness is declared while any governing rule lacks independent evidence — the constitution enforces itself");
  }
}
