package eg.gov.health.platform.transfersafety;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Interfacility transfer safety (module 186). Pre-transfer
 *  stability is verified; escort level, equipment and
 *  medications are specified; receiving acceptance and capacity
 *  are confirmed; the transfer summary and critical results
 *  travel with the patient; and departure, arrival and handover
 *  times are recorded. */
@RestController @RequestMapping("/api/transfer-safety")
public class TransferSafetyController {
  private final TransferAssessmentRepository assessments; private final TransferLogRepository logs; private final AuditService audit;
  public TransferSafetyController(TransferAssessmentRepository assessments, TransferLogRepository logs, AuditService audit) {
    this.assessments=assessments; this.logs=logs; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private TransferAssessment assessment(UUID id) {
    return assessments.findById(id).orElseThrow(() -> BizException.notFound("assessment_not_found","التقييم غير موجود"));
  }

  public record AssessRequest(String patientRef, Boolean patientStable, String escortLevel, String equipmentNeeded,
                              String medicationsNeeded, String receivingFacility, String criticalResultsSummary) {}
  @PostMapping("/assessments")
  Map<String,Object> assess(@RequestBody AssessRequest r) {
    var p = principal();
    var a = assessments.save(new TransferAssessment(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.patientStable(),
      r.escortLevel(), r.equipmentNeeded(), r.medicationsNeeded(), r.receivingFacility(), r.criticalResultsSummary()));
    audit.record("transfer.assessed","transfer_assessment",a.getId().toString(),"{}");
    return Map.of("ok",true,"assessment",a,"escort_adequate",TransferSafetyRules.escortAdequate(r.patientStable(), r.escortLevel()));
  }
  @PostMapping("/assessments/{id}/confirm-stability")
  Map<String,Object> stability(@PathVariable UUID id) {
    principal(); var a = assessment(id); a.confirmStability(); assessments.save(a);
    return Map.of("ok",true,"assessment",a);
  }
  @PostMapping("/assessments/{id}/confirm-acceptance")
  Map<String,Object> acceptance(@PathVariable UUID id) {
    principal(); var a = assessment(id); a.confirmAcceptance(); assessments.save(a);
    audit.record("transfer.receiving.accepted","transfer_assessment",id.toString(),"{}");
    return Map.of("ok",true,"assessment",a);
  }
  @PostMapping("/assessments/{id}/confirm-capacity")
  Map<String,Object> capacity(@PathVariable UUID id) {
    principal(); var a = assessment(id); a.confirmCapacity(); assessments.save(a);
    return Map.of("ok",true,"assessment",a);
  }
  @PostMapping("/assessments/{id}/depart")
  Map<String,Object> depart(@PathVariable UUID id) {
    principal(); var a = assessment(id);
    if (!TransferSafetyRules.departurePermitted(a.isStabilityConfirmed(), a.isReceivingAcceptanceConfirmed(), a.isReceivingCapacityConfirmed()))
      throw BizException.conflict("departure_blocked","لا مغادرة قبل ثلاثة تأكيدات: استقرار المريض للرحلة + قبول الجهة المستقبلة + توفر قدرتها");
    if (!TransferSafetyRules.escortAdequate(a.getPatientStable(), a.getEscortLevel()))
      throw BizException.conflict("escort_inadequate","مريض غير مستقر يسافر بمرافقة طبيب أو فريق عناية — لا بملحوظة مكتوبة");
    a.depart(); assessments.save(a);
    audit.record("transfer.departed","transfer_assessment",id.toString(),"{}");
    return Map.of("ok",true,"assessment",a);
  }
  public record ArriveRequest(OffsetDateTime departedAt, OffsetDateTime arrivedAt, Boolean handoverCompleted, String handoverTo) {}
  @PostMapping("/assessments/{id}/arrive")
  Map<String,Object> arrive(@PathVariable UUID id, @RequestBody ArriveRequest r) {
    var p = principal(); var a = assessment(id);
    if (!"departed".equals(a.getStatus())) throw BizException.conflict("not_departed","وصول على نقل غادر فعليًا");
    if (!Boolean.TRUE.equals(r.handoverCompleted()) || r.handoverTo() == null || r.handoverTo().isBlank())
      throw BizException.conflict("handover_required","النقل يكتمل بتسليم موثق عند الوصول لشخص مسمى — لا عند باب الإرسال");
    var log = logs.save(new TransferLog(UUID.fromString(p.facilityId()), p.governorate(), id, r.departedAt(), r.arrivedAt(), true, r.handoverTo()));
    a.complete(); assessments.save(a);
    audit.record("transfer.completed","transfer_log",log.getId().toString(),"{}");
    return Map.of("ok",true,"log",log,"assessment",a);
  }
}
