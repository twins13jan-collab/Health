package eg.gov.health.platform.programs;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/dialysis")
public class DialysisController {
  private final DialysisSessionRepository repo; private final AuditService audit;
  public DialysisController(DialysisSessionRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record SessionRequest(UUID patientId, String machineCode, OffsetDateTime startsAt, OffsetDateTime endsAt, Double dryWeightKg, Integer ultrafiltrationMl) {}
  @PostMapping("/sessions")
  Map<String,Object> schedule(@RequestBody SessionRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.machineCode()==null||r.startsAt()==null||r.endsAt()==null||!r.endsAt().isAfter(r.startsAt()))
      throw BizException.badRequest("invalid_session","بيانات جلسة غير صحيحة");
    var candidate = new DialysisSession(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.machineCode(), r.startsAt(), r.endsAt(), r.dryWeightKg(), r.ultrafiltrationMl());
    boolean clash = repo.findByFacilityIdAndMachineCode(candidate.getFacilityId(), r.machineCode()).stream().anyMatch(candidate::overlaps);
    if (clash) throw BizException.conflict("machine_busy","الماكينة محجوزة في هذه الفترة — لا حجز مزدوج لماكينات الغسيل (دورة 242)");
    var s = repo.save(candidate);
    audit.record("dialysis.session.scheduled","dialysis_session",s.getId().toString(),"{}");
    return Map.of("ok",true,"session",s);
  }
  @PostMapping("/sessions/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var s = repo.findById(id).orElseThrow(() -> BizException.notFound("session_not_found","الجلسة غير موجودة"));
    String to = body.getOrDefault("status","");
    Map<String,List<String>> flow = Map.of(
      "scheduled", List.of("in_session","missed","cancelled"), "in_session", List.of("completed"),
      "completed", List.of(), "missed", List.of(), "cancelled", List.of());
    if (!flow.getOrDefault(s.getStatus(), List.of()).contains(to))
      throw BizException.conflict("illegal_transition","انتقال غير مسموح: "+s.getStatus()+" -> "+to);
    s.setStatus(to); repo.save(s);
    audit.record("dialysis.session."+to,"dialysis_session",id.toString(),"{}");
    return Map.of("ok",true,"session",s);
  }
  @GetMapping("/sessions")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = repo.findByPatientIdOrderByStartsAtAsc(patientId);
    return Map.of("ok",true,"sessions",rows,"count",rows.size());
  }
}
