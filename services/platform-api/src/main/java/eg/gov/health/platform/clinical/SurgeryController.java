package eg.gov.health.platform.clinical;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/surgery")
public class SurgeryController {
  private final SurgicalCaseRepository repo; private final AuditService audit;
  public SurgeryController(SurgicalCaseRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record CaseRequest(UUID patientId, UUID encounterId, String procedureName, String operatingRoom, OffsetDateTime scheduledAt) {}
  @PostMapping("/cases")
  Map<String,Object> create(@RequestBody CaseRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.procedureName()==null) throw BizException.badRequest("missing_field","patientId و procedureName مطلوبان");
    var c = repo.save(new SurgicalCase(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.encounterId(), r.procedureName(), r.operatingRoom(), r.scheduledAt()));
    audit.record("surgery.case.scheduled","surgical_case",c.getId().toString(),"{}");
    return Map.of("ok",true,"case",c);
  }
  @PostMapping("/cases/{id}/checklist/confirm")
  Map<String,Object> confirm(@PathVariable UUID id, @RequestBody Map<String,Object> body) {
    var c = repo.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","الحالة غير موجودة"));
    @SuppressWarnings("unchecked") var items = (List<String>) body.getOrDefault("items", List.of());
    var allowed = WhoChecklist.STAGES.getOrDefault(c.getChecklistStage(), List.of());
    var invalid = items.stream().filter(i -> !allowed.contains(i)).toList();
    if (!invalid.isEmpty()) throw BizException.badRequest("items_not_in_stage","عناصر لا تنتمي للمرحلة الحالية "+c.getChecklistStage()+": "+invalid);
    c.confirmItems(items); repo.save(c);
    audit.record("surgery.checklist.confirmed","surgical_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c,"stage_complete",WhoChecklist.stageComplete(c.getChecklistStage(), c.confirmedSet()));
  }
  @PostMapping("/cases/{id}/checklist/advance")
  Map<String,Object> advance(@PathVariable UUID id) {
    var c = repo.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","الحالة غير موجودة"));
    var next = WhoChecklist.nextStage(c.getChecklistStage(), c.confirmedSet());
    if (next.isEmpty()) throw BizException.conflict("checklist_incomplete","لا يمكن التقدم: عناصر مرحلة "+c.getChecklistStage()+" غير مكتملة (قائمة WHO، دورة 220)");
    c.advanceTo(next.get()); repo.save(c);
    audit.record("surgery.checklist.advanced","surgical_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c);
  }
  @GetMapping("/cases")
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByScheduledAtAsc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"cases",rows,"count",rows.size());
  }
}
