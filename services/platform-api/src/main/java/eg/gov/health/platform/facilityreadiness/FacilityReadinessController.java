package eg.gov.health.platform.facilityreadiness;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Facility operational readiness for services (module 190).
 *  Completeness of licences, staff, equipment, stock and
 *  infrastructure verified; protocols, training and referral
 *  paths checked; findings, blockers and the remediation plan
 *  managed; the service-activation decision and its authorized
 *  owner governed; re-verification when capability changes or
 *  a failure occurs; and the readiness state, its limits and
 *  its source shown. */
@RestController @RequestMapping("/api/facility-readiness")
public class FacilityReadinessController {
  private final ReadinessAssessmentRepository assessments; private final AuditService audit;
  public FacilityReadinessController(ReadinessAssessmentRepository assessments, AuditService audit) {
    this.assessments=assessments; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ReadinessAssessment assessment(UUID id) {
    return assessments.findById(id).orElseThrow(() -> BizException.notFound("assessment_not_found","التقييم غير موجود"));
  }
  private List<String> blockers(ReadinessAssessment a) {
    return FacilityReadinessRules.blockers(a.isLicencesOk(), a.isStaffOk(), a.isEquipmentOk(), a.isStockOk(),
      a.isInfrastructureOk(), a.isProtocolsOk(), a.isTrainingOk(), a.isReferralPathsOk());
  }

  public record AssessBody(String serviceRef, Boolean licencesOk, Boolean staffOk, Boolean equipmentOk, Boolean stockOk,
                           Boolean infrastructureOk, Boolean protocolsOk, Boolean trainingOk, Boolean referralPathsOk,
                           String remediationPlan) {}
  @PostMapping("/assessments")
  Map<String,Object> create(@RequestBody AssessBody r) {
    var p = principal();
    var a = assessments.save(new ReadinessAssessment(UUID.fromString(p.facilityId()), p.governorate(), r.serviceRef(),
      Boolean.TRUE.equals(r.licencesOk()), Boolean.TRUE.equals(r.staffOk()), Boolean.TRUE.equals(r.equipmentOk()), Boolean.TRUE.equals(r.stockOk()),
      Boolean.TRUE.equals(r.infrastructureOk()), Boolean.TRUE.equals(r.protocolsOk()), Boolean.TRUE.equals(r.trainingOk()),
      Boolean.TRUE.equals(r.referralPathsOk()), r.remediationPlan()));
    return Map.of("ok",true,"assessment",a,"blockers",blockers(a));
  }
  public record DecideBody(String decidedBy) {}
  @PostMapping("/assessments/{id}/activate")
  Map<String,Object> activate(@PathVariable UUID id, @RequestBody DecideBody r) {
    principal(); var a = assessment(id);
    var blockers = blockers(a);
    if (!FacilityReadinessRules.activatable(blockers, r.decidedBy()))
      throw BizException.conflict("not_activatable","تفعيل خدمة يحتاج الفحوص الثمانية كلها وقرار صاحب صلاحية مسمى — العوائق: " + String.join("، ", blockers));
    a.decide("activated", r.decidedBy()); assessments.save(a);
    audit.record("readiness.service.activated","readiness_assessment",id.toString(),"{}");
    return Map.of("ok",true,"assessment",a,"decision","activated");
  }
  @PostMapping("/assessments/{id}/record-not-ready")
  Map<String,Object> notReady(@PathVariable UUID id, @RequestBody DecideBody r) {
    principal(); var a = assessment(id);
    var blockers = blockers(a);
    if (!FacilityReadinessRules.notReadyRecordable(blockers, a.getRemediationPlan()))
      throw BizException.conflict("plan_required","حكم «غير جاهز» يُسجل فقط بعوائق محددة وخطة معالجة — عائق بلا خطة شكوى لا إدارة");
    a.decide("not_ready", r.decidedBy()); assessments.save(a);
    return Map.of("ok",true,"assessment",a,"decision","not_ready","blockers",blockers);
  }
}
