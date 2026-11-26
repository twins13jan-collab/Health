package eg.gov.health.platform.chronicdeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Chronic diseases & population programmes (module 97).
 *  Target patient lists follow declared criteria; follow-up
 *  plans for diabetes, hypertension, cardiac, asthma and renal
 *  disease run; home measurements are source-verified;
 *  appointment, test and medication reminders go out;
 *  complication risk is assessed with early referral; disease
 *  control, dropout and follow-up are measured; and early-
 *  detection programmes carry eligibility criteria. */
@RestController @RequestMapping("/api/chronic-deep")
public class ChronicDeepController {
  private final ChronicEnrollmentRepository enrollments; private final HomeMeasurementRepository measurements; private final AuditService audit;
  public ChronicDeepController(ChronicEnrollmentRepository enrollments, HomeMeasurementRepository measurements, AuditService audit) {
    this.enrollments=enrollments; this.measurements=measurements; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ChronicEnrollment enrollment(UUID id) {
    return enrollments.findById(id).orElseThrow(() -> BizException.notFound("enrollment_not_found","التسجيل غير موجود"));
  }

  public record EnrollBody(String patientRef, String conditionName, String criteriaRef, Boolean meetsCriteria) {}
  @PostMapping("/enrollments")
  Map<String,Object> enroll(@RequestBody EnrollBody r) {
    var p = principal();
    if (!ChronicRules.enrollable(r.criteriaRef(), Boolean.TRUE.equals(r.meetsCriteria())))
      throw BizException.conflict("not_enrollable","تسجيل ببرنامج مزمن يتبع معايير معلنة ويستوفيها — قائمة بلا معايير معلنة قائمة محاباة");
    var e = enrollments.save(new ChronicEnrollment(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.conditionName(),
      r.criteriaRef(), true));
    return Map.of("ok",true,"enrollment",e);
  }
  public record MeasureBody(String metric, Double value, Boolean sourceVerified) {}
  @PostMapping("/enrollments/{id}/measurements")
  Map<String,Object> measure(@PathVariable UUID id, @RequestBody MeasureBody r) {
    var p = principal(); enrollment(id);
    var m = measurements.save(new HomeMeasurement(UUID.fromString(p.facilityId()), p.governorate(), id, r.metric(), r.value(),
      Boolean.TRUE.equals(r.sourceVerified())));
    return Map.of("ok",true,"measurement",m,"counts_toward_control",ChronicRules.countsTowardControl(m.isSourceVerified()),
      "note","an unverified reading is recorded and flagged — it never steers care");
  }
  @GetMapping("/enrollments/{id}/control")
  Map<String,Object> control(@PathVariable UUID id, @RequestParam double targetMax) {
    principal(); enrollment(id);
    var verified = measurements.findByEnrollmentIdOrderByCreatedAtDesc(id).stream()
      .filter(HomeMeasurement::isSourceVerified).toList();
    if (verified.isEmpty())
      return Map.of("ok",true,"enrollment_id",id.toString(),"control","unknown","reason","no verified measurements — control is not guessed");
    double latest = verified.get(0).getValue();
    return Map.of("ok",true,"enrollment_id",id.toString(),"latest_verified_value",latest,
      "controlled",ChronicRules.controlled(latest, targetMax));
  }
}
