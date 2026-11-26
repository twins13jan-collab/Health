package eg.gov.health.platform.anomalyregistry;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** National congenital anomalies registry (module 169). Case
 *  definitions and reporting sources are standardized; pregnancy,
 *  birth and newborn data are linked under permission; cases are
 *  classified confirmed or suspected and the two are never merged
 *  in published figures. Trend analysis describes patterns without
 *  unsupported causal claims, and families are referred to genetic
 *  and rehabilitation services. */
@RestController @RequestMapping("/api/anomaly-registry")
public class AnomalyRegistryController {
  private final AnomalyCaseRepository cases; private final AnomalyLinkageRepository linkages;
  private final FamilyReferralRepository referrals; private final TrendAnalysisRepository trends; private final AuditService audit;
  public AnomalyRegistryController(AnomalyCaseRepository cases, AnomalyLinkageRepository linkages,
                                   FamilyReferralRepository referrals, TrendAnalysisRepository trends, AuditService audit) {
    this.cases=cases; this.linkages=linkages; this.referrals=referrals; this.trends=trends; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private AnomalyCase theCase(UUID id) {
    return cases.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","الحالة غير موجودة"));
  }

  public record CaseRequest(String caseCode, String newbornRef, String anomalyCode, String classification, String sourceType) {}
  @PostMapping("/cases")
  Map<String,Object> register(@RequestBody CaseRequest r) {
    var p = principal();
    var c = cases.save(new AnomalyCase(UUID.fromString(p.facilityId()), p.governorate(), r.caseCode(), r.newbornRef(), r.anomalyCode(),
      r.classification(), r.sourceType(), p.email()));
    audit.record("registry.case.registered","anomaly_case",c.getId().toString(),"{}");
    return Map.of("ok",true,"case",c);
  }
  public record LinkRequest(String linkType, String linkedRef) {}
  @PostMapping("/cases/{id}/linkages")
  Map<String,Object> link(@PathVariable UUID id, @RequestBody LinkRequest r) {
    var p = principal(); theCase(id);
    if (!RegistryRules.linkagePermitted(p.role()))
      throw BizException.forbidden("ربط بيانات الحمل والولادة والمولود مقيد بصلاحيات السجل");
    var l = linkages.save(new AnomalyLinkage(UUID.fromString(p.facilityId()), p.governorate(), id, r.linkType(), r.linkedRef(), p.email()));
    audit.record("registry.case.linked","anomaly_linkage",l.getId().toString(),"{}");
    return Map.of("ok",true,"linkage",l);
  }
  public record ReferralRequest(String service) {}
  @PostMapping("/cases/{id}/family-referrals")
  Map<String,Object> refer(@PathVariable UUID id, @RequestBody ReferralRequest r) {
    var p = principal(); theCase(id);
    var f = referrals.save(new FamilyReferral(UUID.fromString(p.facilityId()), p.governorate(), id, r.service(), p.email()));
    audit.record("registry.family.referred","family_referral",f.getId().toString(),"{}");
    return Map.of("ok",true,"referral",f);
  }
  public record TrendRequest(String period, String region, String note, String causalClaim, String supportingEvidence) {}
  @PostMapping("/trend-analyses")
  Map<String,Object> trend(@RequestBody TrendRequest r) {
    var p = principal();
    if (!RegistryRules.causalClaimPermitted(r.causalClaim(), r.supportingEvidence()))
      throw BizException.conflict("unsupported_causal_claim","الاستنتاج السببي في تحليل الاتجاهات يتطلب دليلًا داعمًا موثقًا — السجل يصف الأنماط ولا يخترع الأسباب");
    UUID fac = UUID.fromString(p.facilityId());
    int confirmed = (int) cases.countByFacilityIdAndClassification(fac, "confirmed");
    int suspected = (int) cases.countByFacilityIdAndClassification(fac, "suspected");
    var t = trends.save(new TrendAnalysis(fac, p.governorate(), r.period(), r.region(), confirmed, suspected, r.note(), r.causalClaim(), r.supportingEvidence()));
    audit.record("registry.trend.published","trend_analysis",t.getId().toString(),"{}");
    return Map.of("ok",true,"trend",t,"counting_rule","headline figures are CONFIRMED cases; suspected cases are reported separately, never merged");
  }
}
