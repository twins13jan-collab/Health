package eg.gov.health.platform.legalcompliance;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Legal requirements & compliance matrix (module 164). A
 *  register of laws, regulations and standards, each linked to
 *  its policy, procedure, control and evidence; effective,
 *  amendment and repeal dates; exceptions with their authority;
 *  and compliance reviews with corrective actions. */
@RestController @RequestMapping("/api/legal-compliance")
public class LegalComplianceController {
  private final LegalRequirementRepository requirements; private final ComplianceExceptionRepository exceptions;
  private final ComplianceReviewRepository reviews; private final AuditService audit;
  public LegalComplianceController(LegalRequirementRepository requirements, ComplianceExceptionRepository exceptions,
                                   ComplianceReviewRepository reviews, AuditService audit) {
    this.requirements=requirements; this.exceptions=exceptions; this.reviews=reviews; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private LegalRequirement requirement(UUID id) {
    return requirements.findById(id).orElseThrow(() -> BizException.notFound("requirement_not_found","المتطلب غير موجود"));
  }

  public record RequirementBody(String title, String sourceType, LocalDate effectiveFrom, LocalDate effectiveTo,
                                String policyRef, String procedureRef, String controlRef, String evidenceRef) {}
  @PostMapping("/requirements")
  Map<String,Object> create(@RequestBody RequirementBody r) {
    var p = principal();
    var req = requirements.save(new LegalRequirement(UUID.fromString(p.facilityId()), p.governorate(), r.title(), r.sourceType(),
      r.effectiveFrom(), r.effectiveTo(), r.policyRef(), r.procedureRef(), r.controlRef(), r.evidenceRef()));
    boolean applicable = LegalRules.requirementApplicable(req.getEffectiveFrom(), req.getEffectiveTo(), LocalDate.now());
    return Map.of("ok",true,"requirement",req,"applicable_today",applicable,
      "matrix_link","requirement -> policy -> procedure -> control -> evidence");
  }
  @GetMapping("/requirements/{id}/applicability")
  Map<String,Object> applicability(@PathVariable UUID id) {
    principal(); var req = requirement(id);
    return Map.of("ok",true,"requirement_id",id.toString(),
      "applicable_today",LegalRules.requirementApplicable(req.getEffectiveFrom(), req.getEffectiveTo(), LocalDate.now()));
  }
  public record ExceptionBody(String justification, String authorityBasis, LocalDate expiresAt) {}
  @PostMapping("/requirements/{id}/exceptions")
  Map<String,Object> exception(@PathVariable UUID id, @RequestBody ExceptionBody r) {
    var p = principal(); requirement(id);
    if (!LegalRules.exceptionValid(r.authorityBasis(), r.expiresAt(), LocalDate.now()))
      throw BizException.badRequest("exception_invalid","استثناء من متطلب قانوني بسند صلاحية وأجل انتهاء — استثناء بلا أجل إلغاء لم يصوت عليه أحد");
    var e = exceptions.save(new ComplianceException(UUID.fromString(p.facilityId()), p.governorate(), id, r.justification(), r.authorityBasis(), r.expiresAt()));
    audit.record("legal.exception.granted","compliance_exception",e.getId().toString(),"{}");
    return Map.of("ok",true,"exception",e);
  }
  public record ReviewBody(Boolean compliant, String correctiveActions) {}
  @PostMapping("/requirements/{id}/reviews")
  Map<String,Object> review(@PathVariable UUID id, @RequestBody ReviewBody r) {
    var p = principal(); requirement(id);
    var rev = reviews.save(new ComplianceReview(UUID.fromString(p.facilityId()), p.governorate(), id, Boolean.TRUE.equals(r.compliant()), r.correctiveActions()));
    audit.record("legal.review.recorded","compliance_review",rev.getId().toString(),"{}");
    return Map.of("ok",true,"review",rev);
  }
  @PostMapping("/reviews/{rid}/corrective-done")
  Map<String,Object> corrective(@PathVariable UUID rid) {
    principal();
    var rev = reviews.findById(rid).orElseThrow(() -> BizException.notFound("review_not_found","المراجعة غير موجودة"));
    rev.completeCorrective(); reviews.save(rev);
    return Map.of("ok",true,"review",rev);
  }
  @PostMapping("/reviews/{rid}/close")
  Map<String,Object> closeReview(@PathVariable UUID rid) {
    principal();
    var rev = reviews.findById(rid).orElseThrow(() -> BizException.notFound("review_not_found","المراجعة غير موجودة"));
    if (!LegalRules.reviewClosable(rev.isCompliant(), rev.isCorrectiveDone()))
      throw BizException.conflict("noncompliant_open","مراجعة عدم امتثال لا تُغلق قبل إتمام الإجراءات التصحيحية");
    rev.close(); reviews.save(rev);
    return Map.of("ok",true,"review",rev);
  }
}
