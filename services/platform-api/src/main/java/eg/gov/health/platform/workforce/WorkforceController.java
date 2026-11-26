package eg.gov.health.platform.workforce;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/workforce")
public class WorkforceController {
  private final StaffMemberRepository staff; private final ShiftRepository shifts; private final AuditService audit;
  public WorkforceController(StaffMemberRepository staff, ShiftRepository shifts, AuditService audit) { this.staff=staff; this.shifts=shifts; this.audit=audit; }
  public record StaffRequest(String userId, String fullName, String profession, String specialty, String licenseNumber) {}
  @PostMapping("/staff")
  Map<String,Object> addStaff(@RequestBody StaffRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.fullName()==null||r.profession()==null) throw BizException.badRequest("missing_field","fullName و profession مطلوبان");
    var s = staff.save(new StaffMember(UUID.fromString(p.facilityId()), p.governorate(), r.userId(), r.fullName(), r.profession(), r.specialty(), r.licenseNumber()));
    return Map.of("ok",true,"staff",s);
  }
  public record ShiftRequest(UUID staffId, String unit, OffsetDateTime startsAt, OffsetDateTime endsAt, String kind) {}
  @PostMapping("/shifts")
  Map<String,Object> schedule(@RequestBody ShiftRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.staffId()==null||r.startsAt()==null||r.endsAt()==null||!r.endsAt().isAfter(r.startsAt()))
      throw BizException.badRequest("invalid_shift","فترة وردية غير صحيحة");
    var member = staff.findById(r.staffId()).orElseThrow(() -> BizException.notFound("staff_not_found","الموظف غير موجود"));
    if (!"active".equals(member.getStatus())) throw BizException.conflict("staff_not_active","لا تُسند ورديات لموظف غير نشط");
    var candidate = new ShiftRules.ShiftView(r.startsAt(), r.endsAt(), "scheduled");
    var existing = shifts.findByStaffId(r.staffId()).stream().map(Shift::toView).toList();
    if (!ShiftRules.isAssignable(candidate, existing))
      throw BizException.conflict("unsafe_roster","تعارض ورديات أو راحة أقل من "+ShiftRules.MIN_REST_HOURS+" ساعة (دورة 231)");
    var s = shifts.save(new Shift(UUID.fromString(p.facilityId()), p.governorate(), r.staffId(), r.unit(), r.startsAt(), r.endsAt(), r.kind()));
    audit.record("workforce.shift.scheduled","shift",s.getId().toString(),"{}");
    return Map.of("ok",true,"shift",s);
  }
  @GetMapping("/staff")
  Map<String,Object> listStaff() {
    var p = TenantContext.get();
    var rows = staff.findByFacilityIdOrderByFullName(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"staff",rows,"count",rows.size());
  }
  @GetMapping("/shifts")
  Map<String,Object> listShifts() {
    var p = TenantContext.get();
    var rows = shifts.findByFacilityIdOrderByStartsAtAsc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"shifts",rows,"count",rows.size());
  }
}
