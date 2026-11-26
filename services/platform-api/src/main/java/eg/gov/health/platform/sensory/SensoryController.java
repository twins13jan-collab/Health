package eg.gov.health.platform.sensory;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Ophthalmology, audiology & ENT (cycle 249). Measurements carry side,
 *  device, method and unit; calibration failure invalidates a result
 *  instead of letting it pass; corrections preserve the original; an
 *  unfit assistive device is never delivered. */
@RestController @RequestMapping("/api/sensory")
public class SensoryController {
  private final SensoryAssessmentRepository assessments; private final OphthalmicExamRepository ophthalmic;
  private final AudiologyTestRepository audiology; private final EntProcedureRepository procedures;
  private final AssistiveDeviceRepository devices; private final AuditService audit;
  public SensoryController(SensoryAssessmentRepository assessments, OphthalmicExamRepository ophthalmic,
                           AudiologyTestRepository audiology, EntProcedureRepository procedures,
                           AssistiveDeviceRepository devices, AuditService audit) {
    this.assessments=assessments; this.ophthalmic=ophthalmic; this.audiology=audiology;
    this.procedures=procedures; this.devices=devices; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record AssessmentRequest(UUID patientId, String organ, String side, String measurements, String method,
                                  String deviceCode, String unit, String measuredBy) {}
  @PostMapping("/assessments")
  Map<String,Object> assess(@RequestBody AssessmentRequest r) {
    var p = principal();
    if (r.patientId()==null||r.organ()==null) throw BizException.badRequest("invalid_assessment","المريض والعضو مطلوبان");
    // A number without side/unit/device/method is never stored.
    if (!SensoryRules.measurementComplete(r.side(), r.unit(), r.deviceCode(), r.method()))
      throw BizException.badRequest("incomplete_measurement","كل قياس مرتبط بالجهة والجهاز والطريقة والوحدة — رقم بلا وحدة لا يُخزن (دورة 249)");
    var a = assessments.save(new SensoryAssessment(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.organ(), r.side(), r.measurements(), r.method(), r.deviceCode(), r.unit(), null, r.measuredBy()));
    audit.record("sensory.test.completed","sensory_assessment",a.getId().toString(),"{}");
    return Map.of("ok",true,"assessment",a);
  }
  /** Correction: the original is preserved (marked corrected) and the new
   *  measurement supersedes it — history is never overwritten. */
  @PostMapping("/assessments/{id}/correct")
  Map<String,Object> correct(@PathVariable UUID id, @RequestBody AssessmentRequest r) {
    var p = principal();
    var orig = assessments.findById(id).orElseThrow(() -> BizException.notFound("assessment_not_found","القياس غير موجود"));
    if (!SensoryRules.measurementComplete(r.side(), r.unit(), r.deviceCode(), r.method()))
      throw BizException.badRequest("incomplete_measurement","التصحيح قياس كامل بنفس القواعد");
    var a = assessments.save(new SensoryAssessment(orig.getFacilityId(), p.governorate(), orig.getPatientId(),
      r.organ(), r.side(), r.measurements(), r.method(), r.deviceCode(), r.unit(), id, r.measuredBy()));
    orig.markCorrected(); assessments.save(orig);
    audit.record("sensory.test.corrected","sensory_assessment",a.getId().toString(),"{}");
    return Map.of("ok",true,"assessment",a,"supersedes",id,"original_preserved",true);
  }

  public record OphthalmicRequest(UUID patientId, String eye, Double visualAcuity, Double iopMmhg, String fundus, String visualField, String examiner) {}
  @PostMapping("/ophthalmic-exams")
  Map<String,Object> ophthalmic(@RequestBody OphthalmicRequest r) {
    var p = principal();
    if (r.patientId()==null||r.eye()==null) throw BizException.badRequest("invalid_exam","المريض والعين مطلوبان");
    if (r.visualAcuity()!=null && !SensoryRules.acuityPlausible(r.visualAcuity()))
      throw BizException.badRequest("acuity_implausible","حدة إبصار خارج النطاق المعقول (0.0–2.0)");
    var e = ophthalmic.save(new OphthalmicExam(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.eye(),
      r.visualAcuity(), r.iopMmhg(), r.fundus(), r.visualField(), r.examiner()));
    audit.record("sensory.ophthalmic.recorded","ophthalmic_exam",e.getId().toString(),"{}");
    return Map.of("ok",true,"exam",e,"iop_suspect",e.isIopSuspect(),
      "note", e.isIopSuspect() ? "implausible IOP flagged suspect — review before acting" : "");
  }

  public record AudiologyRequest(UUID patientId, String ear, String testType, String deviceCode, Boolean calibrationValid,
                                 LocalDate calibrationDate, String resultSummary) {}
  @PostMapping("/audiology-tests")
  Map<String,Object> audiology(@RequestBody AudiologyRequest r) {
    var p = principal();
    if (r.patientId()==null||r.testType()==null) throw BizException.badRequest("invalid_test","المريض ونوع الاختبار مطلوبان");
    var t = audiology.save(new AudiologyTest(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.ear(),
      r.testType(), r.deviceCode(), Boolean.TRUE.equals(r.calibrationValid()), r.calibrationDate(), r.resultSummary()));
    audit.record("sensory.audiology.recorded","audiology_test",t.getId().toString(),"{}");
    return Map.of("ok",true,"test",t,"valid", "valid".equals(t.getStatus()),
      "retest_required", !"valid".equals(t.getStatus()));
  }

  public record EntRequest(UUID patientId, String procedureCode, String site, String side, String anesthesia, String complications) {}
  @PostMapping("/ent-procedures")
  Map<String,Object> ent(@RequestBody EntRequest r) {
    var p = principal();
    if (r.patientId()==null||r.procedureCode()==null) throw BizException.badRequest("invalid_procedure","المريض والإجراء مطلوبان");
    var pr = procedures.save(new EntProcedure(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.procedureCode(), r.site(), r.side(), r.anesthesia(), r.complications()));
    audit.record("sensory.ent.reported","ent_procedure",pr.getId().toString(),"{}");
    return Map.of("ok",true,"procedure",pr,
      "safety_pathway", "complicated".equals(pr.getStatus()) ? "complication pathway: safety review + follow-up" : "");
  }

  public record DeviceRequest(UUID patientId, String deviceType, String specification, LocalDate maintenanceDue) {}
  @PostMapping("/assistive-devices")
  Map<String,Object> device(@RequestBody DeviceRequest r) {
    var p = principal();
    if (r.patientId()==null||r.deviceType()==null) throw BizException.badRequest("invalid_device","المريض ونوع الجهاز مطلوبان");
    var d = devices.save(new AssistiveDevice(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.deviceType(), r.specification(), r.maintenanceDue()));
    audit.record("sensory.device.prescribed","assistive_device",d.getId().toString(),"{}");
    return Map.of("ok",true,"device",d);
  }
  @PostMapping("/assistive-devices/{id}/fit")
  Map<String,Object> fit(@PathVariable UUID id, @RequestBody Map<String,Object> body) {
    var d = devices.findById(id).orElseThrow(() -> BizException.notFound("device_not_found","الجهاز غير موجود"));
    boolean fitted = Boolean.TRUE.equals(body.get("fitted"));
    d.fit(fitted, (String) body.get("notes")); devices.save(d);
    audit.record("sensory.device.fitted","assistive_device",id.toString(),"{}");
    return Map.of("ok",true,"device",d,"fitting_status",d.getFittingStatus());
  }
  @PostMapping("/assistive-devices/{id}/deliver")
  Map<String,Object> deliver(@PathVariable UUID id) {
    var d = devices.findById(id).orElseThrow(() -> BizException.notFound("device_not_found","الجهاز غير موجود"));
    // An unfit device stops delivery and returns to measurement.
    if (!SensoryRules.deliverable(d.getFittingStatus()))
      throw BizException.conflict("not_fitted","جهاز غير ملائم لا يُسلَّم — إيقاف التسليم وإعادة القياس (دورة 249)");
    d.deliver(); devices.save(d);
    audit.record("sensory.device.delivered","assistive_device",id.toString(),"{}");
    return Map.of("ok",true,"device",d);
  }

  @GetMapping("/assessments")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = assessments.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"assessments",rows,"count",rows.size());
  }
}
