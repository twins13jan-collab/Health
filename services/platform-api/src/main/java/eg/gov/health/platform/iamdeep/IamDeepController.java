package eg.gov.health.platform.iamdeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Institutional identity & access management — IAM
 *  (module 63). The lifecycle of human, machine, service and
 *  interface accounts; SSO, MFA, password policies, passkeys
 *  and trusted devices; permissions by role, attribute,
 *  facility, governorate, vendor, purpose and operation;
 *  temporary delegation, entitlement reviews and separation of
 *  duties; joiner, mover and leaver handling with session
 *  revocation; and logging of login attempts, failures,
 *  sensitive changes and permission reviews. */
@RestController @RequestMapping("/api/iam-deep")
public class IamDeepController {
  private final AccessGrantRepository grants; private final EntitlementReviewRepository reviews; private final AuditService audit;
  public IamDeepController(AccessGrantRepository grants, EntitlementReviewRepository reviews, AuditService audit) {
    this.grants=grants; this.reviews=reviews; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record GrantBody(String subjectRef, String roleName, String scope, String purpose, Boolean temporary,
                          OffsetDateTime expiresAt, String approverRef) {}
  @PostMapping("/grants")
  Map<String,Object> grant(@RequestBody GrantBody r) {
    var p = principal();
    if (!IamRules.grantable(r.purpose(), r.scope(), r.subjectRef(), r.approverRef(), Boolean.TRUE.equals(r.temporary()), r.expiresAt()))
      throw BizException.conflict("not_grantable","صلاحية تُمنح لغرض مسمى ونطاق وباعتماد غير صاحبها — منح الذات ليس ضبط وصول بل غيابه، والمؤقت يحتاج انتهاء");
    var g = grants.save(new AccessGrant(UUID.fromString(p.facilityId()), p.governorate(), r.subjectRef(), r.roleName(), r.scope(),
      r.purpose(), Boolean.TRUE.equals(r.temporary()), r.expiresAt(), r.approverRef()));
    audit.record("iam.grant.created","access_grant",g.getId().toString(),"{}");
    return Map.of("ok",true,"grant",g,"active_now",IamRules.active(g.isTemporary(), g.getExpiresAt(), OffsetDateTime.now()));
  }
  @GetMapping("/grants/{id}/active")
  Map<String,Object> active(@PathVariable UUID id) {
    principal();
    var g = grants.findById(id).orElseThrow(() -> BizException.notFound("grant_not_found","المنح غير موجود"));
    return Map.of("ok",true,"grant",g,"active_now",IamRules.active(g.isTemporary(), g.getExpiresAt(), OffsetDateTime.now()),
      "note","an expired temporary grant is inactive by computation — nobody has to remember to revoke it");
  }
  public record ReviewBody(String subjectRef, String reviewerRef, String outcome) {}
  @PostMapping("/entitlement-reviews")
  Map<String,Object> review(@RequestBody ReviewBody r) {
    var p = principal();
    if (r.subjectRef() != null && r.subjectRef().equals(r.reviewerRef()))
      throw BizException.forbidden("مراجعة الاستحقاق لا يجريها صاحبها — فصل الواجبات");
    var rev = reviews.save(new EntitlementReview(UUID.fromString(p.facilityId()), p.governorate(), r.subjectRef(), r.reviewerRef(), r.outcome()));
    return Map.of("ok",true,"review",rev);
  }
}
