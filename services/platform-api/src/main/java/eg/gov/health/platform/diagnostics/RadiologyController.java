package eg.gov.health.platform.diagnostics;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.patient.PatientRepository;
import eg.gov.health.platform.radiologydeep.ImagingCriticalResultRepository;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
@RestController @RequestMapping("/api/radiology")
public class RadiologyController {
  private final RadiologyOrderRepository repo; private final AuditService audit;
  private final PatientRepository patients; private final ImagingCriticalResultRepository criticals;
  public RadiologyController(RadiologyOrderRepository repo, AuditService audit,
                             PatientRepository patients, ImagingCriticalResultRepository criticals) {
    this.repo=repo; this.audit=audit; this.patients=patients; this.criticals=criticals;
  }
  static final List<String> MODALITIES = List.of("XR","CT","MRI","US","MAMMO","ANGIO");
  static final List<String> FLOW = List.of("ordered","scheduled","in_progress","imaged","reported","verified","cancelled");
  public record RadRequest(UUID patientId, UUID encounterId, String modality, String bodyPart, String clinicalQuestion, String priority) {}
  @PostMapping("/orders")
  Map<String,Object> create(@RequestBody RadRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||!MODALITIES.contains(r.modality()==null?"":r.modality()))
      throw BizException.badRequest("invalid_modality","وسيلة تصوير غير معروفة");
    if (r.clinicalQuestion()==null||r.clinicalQuestion().isBlank())
      throw BizException.badRequest("missing_clinical_question","السؤال السريري إلزامي — لا تصوير بلا مبرر (تبرير الإشعاع)");
    var o = repo.save(new RadiologyOrder(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.encounterId(), r.modality(), r.bodyPart(), r.clinicalQuestion(), r.priority(), p.userId()));
    audit.record("radiology.order.created","radiology_order",o.getId().toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  @PostMapping("/orders/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var o = repo.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","الطلب غير موجود"));
    String to = body.getOrDefault("status","");
    int cur = FLOW.indexOf(o.getStatus()); int nxt = FLOW.indexOf(to);
    if (nxt < 0) throw BizException.badRequest("invalid_status","حالة غير معروفة");
    if (!("cancelled".equals(to) && cur < FLOW.indexOf("verified")) && nxt != cur + 1)
      throw BizException.conflict("illegal_transition","المسار التسلسلي إلزامي: "+o.getStatus()+" -> "+to+" مرفوض");
    o.setStatus(to); repo.save(o);
    audit.record("radiology.order."+to,"radiology_order",id.toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  @PostMapping("/orders/{id}/report")
  Map<String,Object> report(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = TenantContext.get();
    var o = repo.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","الطلب غير موجود"));
    if (!"imaged".equals(o.getStatus())) throw BizException.conflict("not_imaged","التقرير بعد إتمام التصوير فقط");
    if (body.getOrDefault("report_text","").isBlank()) throw BizException.badRequest("empty_report","نص التقرير مطلوب");
    o.report(body.get("report_text"), p==null?null:p.userId()); repo.save(o);
    audit.record("radiology.order.reported","radiology_order",id.toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  /** Department worklist board (Phase 4.8): the acquisition queue and
   *  the reporting queue, each ordered by RadiologyBoardRules
   *  (priority, then longest waiting), plus unacknowledged critical
   *  results from the deep layer — a critical finding that nobody has
   *  acknowledged stays at the top of the department's attention. */
  @GetMapping("/board")
  Map<String,Object> board() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    UUID fac = UUID.fromString(p.facilityId());
    var now = OffsetDateTime.now();
    var today = LocalDate.now(ZoneId.of("Africa/Cairo"));
    var all = repo.findByFacilityIdOrderByCreatedAtDesc(fac);
    List<Map<String,Object>> acquisition = new ArrayList<>(), reporting = new ArrayList<>(), verification = new ArrayList<>();
    long inProgress = 0, overdue = 0, verifiedToday = 0;
    Map<String,Long> waitingByModality = new LinkedHashMap<>();
    for (var o : all.stream().filter(o -> "acquisition".equals(RadiologyBoardRules.stageOf(o.getStatus())))
        .sorted(RadiologyBoardRules.acquisitionOrder()).toList()) {
      if ("in_progress".equals(o.getStatus())) inProgress++;
      waitingByModality.merge(o.getModality(), 1L, Long::sum);
      acquisition.add(row(o, now));
    }
    for (var o : all.stream().filter(o -> "reporting".equals(RadiologyBoardRules.stageOf(o.getStatus())))
        .sorted(RadiologyBoardRules.reportingOrder()).toList()) {
      var r = row(o, now);
      long mins = o.getUpdatedAt()==null ? 0 : Duration.between(o.getUpdatedAt(), now).toMinutes();
      boolean od = RadiologyBoardRules.reportOverdue(o.getStatus(), o.getPriority(), mins);
      r.put("minutes_since_imaged", mins);
      r.put("report_overdue", od);
      r.put("sla_minutes", RadiologyBoardRules.reportingSlaMinutes(o.getPriority()));
      if (od) overdue++;
      reporting.add(r);
    }
    for (var o : all) {
      if ("verification".equals(RadiologyBoardRules.stageOf(o.getStatus()))) verification.add(row(o, now));
      if ("verified".equals(o.getStatus()) && o.getUpdatedAt()!=null
          && o.getUpdatedAt().atZoneSameInstant(ZoneId.of("Africa/Cairo")).toLocalDate().equals(today)) verifiedToday++;
    }
    List<Map<String,Object>> critRows = new ArrayList<>();
    for (var c : criticals.findByFacilityIdAndAcknowledgedFalseOrderByCreatedAtAsc(fac)) {
      Map<String,Object> r = new LinkedHashMap<>();
      r.put("id", c.getId()); r.put("order_id", c.getOrderId()); r.put("finding", c.getFinding());
      r.put("created_at", c.getCreatedAt());
      r.put("minutes_waiting", c.getCreatedAt()==null ? 0 : Duration.between(c.getCreatedAt(), now).toMinutes());
      critRows.add(r);
    }
    Map<String,Object> summary = new LinkedHashMap<>();
    summary.put("waiting_imaging", acquisition.size());
    summary.put("in_progress", inProgress);
    summary.put("awaiting_report", reporting.size());
    summary.put("report_overdue", overdue);
    summary.put("awaiting_verification", verification.size());
    summary.put("verified_today", verifiedToday);
    summary.put("unacknowledged_criticals", critRows.size());
    summary.put("waiting_by_modality", waitingByModality);
    Map<String,Object> out = new LinkedHashMap<>();
    out.put("ok", true); out.put("acquisition", acquisition); out.put("reporting", reporting);
    out.put("verification", verification); out.put("criticals", critRows); out.put("summary", summary);
    return out;
  }
  private Map<String,Object> row(RadiologyOrder o, OffsetDateTime now) {
    Map<String,Object> r = new LinkedHashMap<>();
    r.put("id", o.getId()); r.put("patient_id", o.getPatientId());
    r.put("patient_name", patients.findById(o.getPatientId()).map(pt -> pt.getFullName()).orElse(""));
    r.put("modality", o.getModality()); r.put("body_part", o.getBodyPart());
    r.put("clinical_question", o.getClinicalQuestion()); r.put("priority", o.getPriority());
    r.put("status", o.getStatus()); r.put("stage", RadiologyBoardRules.stageOf(o.getStatus()));
    r.put("created_at", o.getCreatedAt());
    r.put("waiting_minutes", o.getCreatedAt()==null ? 0 : Duration.between(o.getCreatedAt(), now).toMinutes());
    if (o.getReportText()!=null) r.put("report_text", o.getReportText());
    return r;
  }
  @GetMapping("/orders")
  Map<String,Object> list(@RequestParam(required=false) UUID patientId) {
    var p = TenantContext.get();
    var rows = patientId!=null ? repo.findByPatientIdOrderByCreatedAtDesc(patientId) : repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"orders",rows,"count",rows.size());
  }
}
