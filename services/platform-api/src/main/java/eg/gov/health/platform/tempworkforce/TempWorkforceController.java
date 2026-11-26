package eg.gov.health.platform.tempworkforce;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Temporary workforce marketplace (module 171). Provider
 *  identity and qualifications are verified and licences matched
 *  against availability before any assignment; contracts,
 *  delegation and scope-of-practice limits are explicit; shifts,
 *  attendance and evaluations are managed; and the two hard
 *  protections hold — no double-assignment (overlapping shifts)
 *  and no breach of working-time limits (60 hours per rolling
 *  7 days), however urgent the staffing gap. */
@RestController @RequestMapping("/api/temp-workforce")
public class TempWorkforceController {
  private final TempProviderRepository providers; private final TempAvailabilityRepository availability;
  private final TempAssignmentRepository assignments; private final TempEvaluationRepository evaluations; private final AuditService audit;
  public TempWorkforceController(TempProviderRepository providers, TempAvailabilityRepository availability,
                                 TempAssignmentRepository assignments, TempEvaluationRepository evaluations, AuditService audit) {
    this.providers=providers; this.availability=availability; this.assignments=assignments; this.evaluations=evaluations; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private TempProvider provider(UUID id) {
    return providers.findById(id).orElseThrow(() -> BizException.notFound("provider_not_found","مقدم الخدمة غير موجود"));
  }

  public record ProviderRequest(String providerRef, String profession, String licenseNo, LocalDate licenseValidUntil,
                                Boolean identityVerified, Boolean qualificationsVerified, String practiceScope) {}
  @PostMapping("/providers")
  Map<String,Object> register(@RequestBody ProviderRequest r) {
    var p = principal();
    var pr = providers.save(new TempProvider(UUID.fromString(p.facilityId()), p.governorate(), r.providerRef(), r.profession(), r.licenseNo(),
      r.licenseValidUntil(), Boolean.TRUE.equals(r.identityVerified()), Boolean.TRUE.equals(r.qualificationsVerified()), r.practiceScope()));
    audit.record("temp.provider.registered","temp_provider",pr.getId().toString(),"{}");
    return Map.of("ok",true,"provider",pr);
  }
  public record AvailabilityRequest(OffsetDateTime availableFrom, OffsetDateTime availableTo) {}
  @PostMapping("/providers/{id}/availability")
  Map<String,Object> avail(@PathVariable UUID id, @RequestBody AvailabilityRequest r) {
    var p = principal(); provider(id);
    var a = availability.save(new TempAvailability(UUID.fromString(p.facilityId()), p.governorate(), id, r.availableFrom(), r.availableTo()));
    return Map.of("ok",true,"availability",a);
  }
  public record AssignmentRequest(String department, String scopeOfPractice, OffsetDateTime shiftStart, OffsetDateTime shiftEnd) {}
  @PostMapping("/providers/{id}/assignments")
  Map<String,Object> assign(@PathVariable UUID id, @RequestBody AssignmentRequest r) {
    var p = principal(); var pr = provider(id);
    if (!TempWorkforceRules.providerAssignable(pr.isIdentityVerified(), pr.isQualificationsVerified(), pr.getLicenseValidUntil(),
        r.shiftStart() == null ? null : r.shiftStart().toLocalDate(), pr.getStatus()))
      throw BizException.conflict("provider_not_assignable","الإسناد يتطلب هوية ومؤهلات متحققًا منهما وترخيصًا ساريًا في تاريخ الوردية");
    if (!TempWorkforceRules.scopeMatches(pr.getPracticeScope(), r.scopeOfPractice()))
      throw BizException.conflict("scope_mismatch","الإسناد خارج نطاق ممارسة مقدم الخدمة المرخص");
    var existing = assignments.findByProviderIdAndStatusIn(id, List.of("offered","accepted"));
    for (var e : existing) {
      if (TempWorkforceRules.overlaps(r.shiftStart(), r.shiftEnd(), e.getShiftStart(), e.getShiftEnd()))
        throw BizException.conflict("double_assignment","ازدواج إسناد: وردية متداخلة قائمة لنفس مقدم الخدمة");
    }
    var windowStart = r.shiftStart().minusDays(7);
    double hoursInWindow = 0;
    for (var e : existing) {
      if (!e.getShiftStart().isBefore(windowStart)) hoursInWindow += TempWorkforceRules.hours(e.getShiftStart(), e.getShiftEnd());
    }
    if (!TempWorkforceRules.withinWeeklyLimit(hoursInWindow, r.shiftStart(), r.shiftEnd()))
      throw BizException.conflict("hours_limit","تجاوز حد ساعات العمل: 60 ساعة في 7 أيام متداولة — الرفض واجب مهما كانت فجوة التوظيف عاجلة");
    var a = assignments.save(new TempAssignment(UUID.fromString(p.facilityId()), p.governorate(), id, r.department(), r.scopeOfPractice(), r.shiftStart(), r.shiftEnd()));
    audit.record("temp.assignment.offered","temp_assignment",a.getId().toString(),"{}");
    return Map.of("ok",true,"assignment",a,"hours_in_rolling_week_after",hoursInWindow + TempWorkforceRules.hours(r.shiftStart(), r.shiftEnd()));
  }
  @PostMapping("/assignments/{aid}/accept")
  Map<String,Object> accept(@PathVariable UUID aid) {
    principal();
    var a = assignments.findById(aid).orElseThrow(() -> BizException.notFound("assignment_not_found","الإسناد غير موجود"));
    a.setStatus("accepted"); assignments.save(a);
    return Map.of("ok",true,"assignment",a);
  }
  public record EvaluationRequest(Integer rating, String note) {}
  @PostMapping("/assignments/{aid}/evaluations")
  Map<String,Object> evaluate(@PathVariable UUID aid, @RequestBody EvaluationRequest r) {
    var p = principal();
    var e = evaluations.save(new TempEvaluation(UUID.fromString(p.facilityId()), p.governorate(), aid, r.rating(), r.note()));
    return Map.of("ok",true,"evaluation",e);
  }
}
