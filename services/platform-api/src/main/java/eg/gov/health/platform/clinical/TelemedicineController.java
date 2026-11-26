package eg.gov.health.platform.clinical;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/telemedicine")
public class TelemedicineController {
  private final TeleconsultationRepository repo; private final AuditService audit;
  public TelemedicineController(TeleconsultationRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record TeleRequest(UUID patientId, String consultantName, String specialty, OffsetDateTime scheduledAt) {}
  @PostMapping("/consultations")
  Map<String,Object> book(@RequestBody TeleRequest r) {
    var p = TenantContext.get();
    if (r.patientId()==null||r.consultantName()==null||r.scheduledAt()==null) throw BizException.badRequest("missing_field","بيانات الاستشارة ناقصة");
    var t = repo.save(new Teleconsultation(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.consultantName(), r.specialty(), r.scheduledAt()));
    audit.record("telemedicine.booked","teleconsultation",t.getId().toString(),"{}");
    return Map.of("ok",true,"consultation",t);
  }
  @PostMapping("/consultations/{id}/complete")
  Map<String,Object> complete(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var t = repo.findById(id).orElseThrow(() -> BizException.notFound("consultation_not_found","الاستشارة غير موجودة"));
    if (!"in_session".equals(t.getStatus()) && !"scheduled".equals(t.getStatus())) throw BizException.conflict("bad_state","الإكمال من جلسة قائمة فقط");
    if (body.getOrDefault("notes","").isBlank()) throw BizException.badRequest("notes_required","ملاحظات الجلسة جزء من السجل الطبي — إلزامية");
    t.complete(body.get("notes")); repo.save(t);
    audit.record("telemedicine.completed","teleconsultation",id.toString(),"{}");
    return Map.of("ok",true,"consultation",t);
  }
  @PostMapping("/consultations/{id}/escalate")
  Map<String,Object> escalate(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var t = repo.findById(id).orElseThrow(() -> BizException.notFound("consultation_not_found","الاستشارة غير موجودة"));
    if (body.getOrDefault("reason","").isBlank()) throw BizException.badRequest("reason_required","سبب التحويل لحضوري موثق إلزاميًا");
    t.setStatus("escalated_to_in_person"); repo.save(t);
    audit.record("telemedicine.escalated","teleconsultation",id.toString(),"{}");
    return Map.of("ok",true,"consultation",t);
  }
  @GetMapping("/consultations")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = repo.findByPatientIdOrderByScheduledAtAsc(patientId);
    return Map.of("ok",true,"consultations",rows,"count",rows.size());
  }
}
