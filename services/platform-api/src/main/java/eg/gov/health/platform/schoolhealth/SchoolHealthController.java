package eg.gov.health.platform.schoolhealth;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** School & university health (module 143). Screenings,
 *  vaccinations, education and referrals for students — standing
 *  on guardian consent: recorded on the record for screening, and
 *  verified AGAIN at the moment of vaccination. Chronic conditions,
 *  allergies and emergencies are managed per student; outbreaks
 *  and gatherings per institution. Reports to institutions are
 *  aggregate-only with a minimum group size of ten — below that, a
 *  "statistic" is a disclosure of a minor. Mental-health support
 *  stays confidential to counsellor and administration roles. */
@RestController @RequestMapping("/api/school-health")
public class SchoolHealthController {
  private final StudentHealthRecordRepository students; private final SchoolScreeningRepository screenings;
  private final SchoolVaccinationRepository vaccinations; private final SchoolHealthEventRepository events; private final AuditService audit;
  public SchoolHealthController(StudentHealthRecordRepository students, SchoolScreeningRepository screenings,
                                SchoolVaccinationRepository vaccinations, SchoolHealthEventRepository events, AuditService audit) {
    this.students=students; this.screenings=screenings; this.vaccinations=vaccinations; this.events=events; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private StudentHealthRecord student(UUID id) {
    return students.findById(id).orElseThrow(() -> BizException.notFound("student_not_found","سجل الطالب غير موجود"));
  }

  public record StudentRequest(String studentCode, String institutionRef, Boolean guardianConsent, String chronicConditions, String allergyNotes) {}
  @PostMapping("/students")
  Map<String,Object> enroll(@RequestBody StudentRequest r) {
    var p = principal();
    var s = students.save(new StudentHealthRecord(UUID.fromString(p.facilityId()), p.governorate(), r.studentCode(), r.institutionRef(),
      Boolean.TRUE.equals(r.guardianConsent()), r.chronicConditions(), r.allergyNotes()));
    audit.record("school.student.enrolled","student_health_record",s.getId().toString(),"{}");
    return Map.of("ok",true,"student",s);
  }
  public record ScreeningRequest(String screeningType, String result, Boolean referralNeeded, String referralRef, Boolean guardianNotified) {}
  @PostMapping("/students/{id}/screenings")
  Map<String,Object> screen(@PathVariable UUID id, @RequestBody ScreeningRequest r) {
    var p = principal(); var s = student(id);
    if (!SchoolHealthRules.screeningPermitted(s.isGuardianConsent()))
      throw BizException.conflict("guardian_consent","فحص القاصر يتطلب موافقة ولي الأمر المسجلة");
    boolean referralNeeded = Boolean.TRUE.equals(r.referralNeeded());
    if (!SchoolHealthRules.referralComplete(referralNeeded, Boolean.TRUE.equals(r.guardianNotified())))
      throw BizException.conflict("guardian_notification","إحالة من فحص غير طبيعي لا تكتمل قبل إخطار ولي الأمر");
    var sc = screenings.save(new SchoolScreening(UUID.fromString(p.facilityId()), p.governorate(), id, r.screeningType(), r.result(),
      referralNeeded, r.referralRef(), Boolean.TRUE.equals(r.guardianNotified())));
    audit.record("school.screening.done","school_screening",sc.getId().toString(),"{}");
    return Map.of("ok",true,"screening",sc);
  }
  public record VaccinationRequest(String vaccine, String dose, String batch, Boolean consentVerified) {}
  @PostMapping("/students/{id}/vaccinations")
  Map<String,Object> vaccinate(@PathVariable UUID id, @RequestBody VaccinationRequest r) {
    var p = principal(); var s = student(id);
    if (!SchoolHealthRules.vaccinationPermitted(s.isGuardianConsent(), Boolean.TRUE.equals(r.consentVerified())))
      throw BizException.conflict("consent_verification","التطعيم يتطلب موافقة ولي الأمر المسجلة والتحقق منها مجددًا لحظة الإعطاء");
    var v = vaccinations.save(new SchoolVaccination(UUID.fromString(p.facilityId()), p.governorate(), id, r.vaccine(), r.dose(), r.batch(), true));
    audit.record("school.vaccination.given","school_vaccination",v.getId().toString(),"{}");
    return Map.of("ok",true,"vaccination",v);
  }
  public record EventRequest(String institutionRef, String eventType, String description, String actionTaken) {}
  @PostMapping("/events")
  Map<String,Object> event(@RequestBody EventRequest r) {
    var p = principal();
    var e = events.save(new SchoolHealthEvent(UUID.fromString(p.facilityId()), p.governorate(), r.institutionRef(), r.eventType(), r.description(), r.actionTaken()));
    audit.record("school.event.recorded","school_health_event",e.getId().toString(),"{}");
    return Map.of("ok",true,"event",e,"confidential","mental_health_support".equals(r.eventType()));
  }
  @GetMapping("/events/{id}")
  Map<String,Object> getEvent(@PathVariable UUID id) {
    var p = principal();
    var e = events.findById(id).orElseThrow(() -> BizException.notFound("event_not_found","الحدث غير موجود"));
    // Minors' mental-health support is confidential: counsellor and administration roles only.
    if (!SchoolHealthRules.confidentialVisible(e.getEventType(), p.role()))
      throw BizException.forbidden("دعم الصحة النفسية للقاصرين سري — مقيد لأدوار الإرشاد والإدارة");
    return Map.of("ok",true,"event",e);
  }
  @GetMapping("/institutions/{ref}/aggregate")
  Map<String,Object> aggregate(@PathVariable String ref) {
    principal();
    long n = students.countByInstitutionRef(ref);
    // Aggregate-only reporting with k-anonymity: a group under ten is a disclosure, not a statistic.
    if (!SchoolHealthRules.aggregatePublishable(n))
      throw BizException.forbidden("مجموعة أصغر من عشرة طلاب لا تُنشر مجمعة — الإحصائية الصغيرة كشف لبيانات قاصرين");
    return Map.of("ok",true,"institution_ref",ref,"students",n,"disclosure","aggregate counts only — no individual student data");
  }
}
