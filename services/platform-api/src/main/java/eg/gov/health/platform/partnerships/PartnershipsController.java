package eg.gov.health.platform.partnerships;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.time.*; import java.util.*;
/** Partnerships & external service providers (module 132). A
 *  registry of partners, contractors and concessionaires with their
 *  contracts and SLAs. A contract activates only with privacy AND
 *  security terms agreed, a defined access scope and purpose, and a
 *  valid period. Access grants are purpose-bound and time-boxed,
 *  issued only under an active contract for an active partner — and
 *  terminating the relationship revokes every grant at once.
 *  Performance and incidents are reviewed against the SLA;
 *  conflicts of interest and ownership changes stay on the record. */
@RestController @RequestMapping("/api/partnerships")
public class PartnershipsController {
  private final PartnerOrganizationRepository partners; private final PartnerContractRepository contracts;
  private final PartnerAccessGrantRepository grants; private final PartnerPerformanceReviewRepository reviews; private final AuditService audit;
  public PartnershipsController(PartnerOrganizationRepository partners, PartnerContractRepository contracts,
                                PartnerAccessGrantRepository grants, PartnerPerformanceReviewRepository reviews, AuditService audit) {
    this.partners=partners; this.contracts=contracts; this.grants=grants; this.reviews=reviews; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private PartnerOrganization partner(UUID id) {
    return partners.findById(id).orElseThrow(() -> BizException.notFound("partner_not_found","الشريك غير موجود"));
  }

  public record PartnerRequest(String name, String partnerType, Boolean coiDeclared, String ownershipNote) {}
  @PostMapping("/")
  Map<String,Object> create(@RequestBody PartnerRequest r) {
    var p = principal();
    var o = partners.save(new PartnerOrganization(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.partnerType(),
      Boolean.TRUE.equals(r.coiDeclared()), r.ownershipNote()));
    audit.record("partner.registered","partner_organization",o.getId().toString(),"{}");
    return Map.of("ok",true,"partner",o);
  }
  public record ContractRequest(String scope, String accessScope, String purpose, LocalDate startsOn, LocalDate endsOn,
                                String slaRef, Boolean privacyTerms, Boolean securityTerms) {}
  @PostMapping("/{id}/contracts")
  Map<String,Object> contract(@PathVariable UUID id, @RequestBody ContractRequest r) {
    var p = principal(); var o = partner(id);
    var c = contracts.save(new PartnerContract(UUID.fromString(p.facilityId()), p.governorate(), id, r.scope(), r.accessScope(),
      r.purpose(), r.startsOn(), r.endsOn(), r.slaRef(), Boolean.TRUE.equals(r.privacyTerms()), Boolean.TRUE.equals(r.securityTerms())));
    audit.record("partner.contract.drafted","partner_contract",c.getId().toString(),"{}");
    return Map.of("ok",true,"contract",c);
  }
  @PostMapping("/contracts/{cid}/activate")
  Map<String,Object> activate(@PathVariable UUID cid) {
    principal();
    var c = contracts.findById(cid).orElseThrow(() -> BizException.notFound("contract_not_found","العقد غير موجود"));
    if (!PartnershipRules.contractActivatable(c.isPrivacyTerms(), c.isSecurityTerms(), c.getAccessScope(), c.getPurpose(), c.getStartsOn(), c.getEndsOn()))
      throw BizException.conflict("contract_incomplete","التفعيل يتطلب شروط خصوصية وأمن متفقًا عليها ونطاق وصول وغرضًا محددين ومدة صالحة");
    c.setStatus("active"); contracts.save(c);
    audit.record("partner.contract.activated","partner_contract",cid.toString(),"{}");
    return Map.of("ok",true,"contract",c);
  }
  public record GrantRequest(UUID contractId, String systemRef, String accessLevel, String purpose, OffsetDateTime expiresAt) {}
  @PostMapping("/{id}/access-grants")
  Map<String,Object> grant(@PathVariable UUID id, @RequestBody GrantRequest r) {
    var p = principal(); var o = partner(id);
    var c = contracts.findById(r.contractId()).orElseThrow(() -> BizException.notFound("contract_not_found","العقد غير موجود"));
    if (!PartnershipRules.grantPermitted(o.getStatus(), c.getStatus(), r.expiresAt(), OffsetDateTime.now()))
      throw BizException.conflict("grant_blocked","الوصول يتطلب شريكًا نشطًا وعقدًا نشطًا وانتهاءً مستقبليًا محددًا");
    var g = grants.save(new PartnerAccessGrant(UUID.fromString(p.facilityId()), p.governorate(), id, r.systemRef(), r.accessLevel(), r.purpose(), p.email(), r.expiresAt()));
    audit.record("partner.access.granted","partner_access_grant",g.getId().toString(),"{}");
    return Map.of("ok",true,"grant",g);
  }
  @PostMapping("/{id}/terminate")
  Map<String,Object> terminate(@PathVariable UUID id) {
    principal(); var o = partner(id);
    o.setStatus("terminated"); partners.save(o);
    int revoked = 0;
    for (var g : grants.findByPartnerId(id)) if ("active".equals(g.getStatus())) { g.revoke(); grants.save(g); revoked++; }
    for (var c : contracts.findByPartnerId(id)) if ("active".equals(c.getStatus())) { c.setStatus("terminated"); contracts.save(c); }
    audit.record("partner.terminated","partner_organization",id.toString(),"{}");
    return Map.of("ok",true,"partner",o,"access_grants_revoked",revoked,"effect","termination revokes all access immediately");
  }
  public record ReviewRequest(String period, BigDecimal slaScore, Integer incidentsCount, String auditResult) {}
  @PostMapping("/{id}/reviews")
  Map<String,Object> review(@PathVariable UUID id, @RequestBody ReviewRequest r) {
    var p = principal(); var o = partner(id);
    var rv = reviews.save(new PartnerPerformanceReview(UUID.fromString(p.facilityId()), p.governorate(), id, r.period(), r.slaScore(),
      r.incidentsCount(), r.auditResult(), p.email()));
    boolean flagged = PartnershipRules.reviewFlagsAction(r.slaScore() == null ? 100 : r.slaScore().doubleValue(), r.incidentsCount() == null ? 0 : r.incidentsCount());
    audit.record("partner.review.recorded","partner_performance_review",rv.getId().toString(),"{}");
    return Map.of("ok",true,"review",rv,"action_flagged",flagged,
      "flag_reason",flagged ? "SLA under 80 or 3+ incidents — corrective action required" : "within SLA");
  }
}
