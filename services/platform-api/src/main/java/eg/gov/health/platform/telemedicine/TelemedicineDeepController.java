package eg.gov.health.platform.telemedicine;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Telemedicine & digital care (module 112). Session booking,
 *  verification of both parties and consent; files and
 *  measurements shared; the limits of remote examination and
 *  the in-person decision documented; prescriptions and
 *  referrals issued per regulations; connection quality and
 *  dropouts managed; the session protected and unauthorized
 *  recording prevented; and home devices linked with their
 *  measurement source verified. */
@RestController @RequestMapping("/api/telemedicine")
public class TelemedicineDeepController {
  private final TeleSessionRepository sessions; private final TelePrescriptionRepository prescriptions; private final AuditService audit;
  public TelemedicineDeepController(TeleSessionRepository sessions, TelePrescriptionRepository prescriptions, AuditService audit) {
    this.sessions=sessions; this.prescriptions=prescriptions; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private TeleSession session(UUID id) {
    return sessions.findById(id).orElseThrow(() -> BizException.notFound("session_not_found","الجلسة غير موجودة"));
  }

  public record SessionBody(String patientRef, String clinicianRef, Boolean partiesVerified, Boolean consentObtained,
                            Boolean examLimitationsDocumented, String inPersonDecision, Boolean recordingAuthorized) {}
  @PostMapping("/sessions")
  Map<String,Object> create(@RequestBody SessionBody r) {
    var p = principal();
    var s = sessions.save(new TeleSession(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.clinicianRef(),
      Boolean.TRUE.equals(r.partiesVerified()), Boolean.TRUE.equals(r.consentObtained()), Boolean.TRUE.equals(r.examLimitationsDocumented()),
      r.inPersonDecision(), Boolean.TRUE.equals(r.recordingAuthorized())));
    return Map.of("ok",true,"session",s);
  }
  @PostMapping("/sessions/{id}/start")
  Map<String,Object> start(@PathVariable UUID id) {
    principal(); var s = session(id);
    if (!TeleRules.startable(s.isPartiesVerified(), s.isConsentObtained()))
      throw BizException.conflict("not_startable","جلسة عن بعد تبدأ فقط بطرفين موثقين وموافقة — غريب غير موثق على أي طرف ليس استشارة");
    s.activate(); sessions.save(s);
    return Map.of("ok",true,"session",s);
  }
  @PostMapping("/sessions/{id}/record")
  Map<String,Object> record(@PathVariable UUID id) {
    principal(); var s = session(id);
    if (!s.isRecordingAuthorized()) {
      audit.record("telemedicine.recording.blocked","tele_session",id.toString(),"{}");
      throw BizException.forbidden("تسجيل جلسة بلا تصريح ممنوع — الحماية جزء من الرعاية");
    }
    return Map.of("ok",true,"recording",true);
  }
  @PostMapping("/sessions/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal(); var s = session(id);
    if (!TeleRules.closable(s.isExamLimitationsDocumented(), s.getInPersonDecision()))
      throw BizException.conflict("not_closable","جلسة عن بعد لا تُغلق قبل توثيق حدود الفحص عن بعد وقرار الحضور الشخصي — رعاية تدعي بلا حدود هي الخطر");
    s.close(); sessions.save(s);
    return Map.of("ok",true,"session",s);
  }
  public record RxBody(String drug, Boolean regulationCompliant) {}
  @PostMapping("/sessions/{id}/prescriptions")
  Map<String,Object> prescribe(@PathVariable UUID id, @RequestBody RxBody r) {
    var p = principal(); session(id);
    if (!Boolean.TRUE.equals(r.regulationCompliant()))
      throw BizException.conflict("non_compliant_prescription","وصفة عن بعد تخضع للوائح أي وصفة — البعد لا يخفف القاعدة");
    var rx = prescriptions.save(new TelePrescription(UUID.fromString(p.facilityId()), p.governorate(), id, r.drug(), true));
    return Map.of("ok",true,"prescription",rx);
  }
}
