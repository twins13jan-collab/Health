package eg.gov.health.platform.proms;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.time.*; import java.util.*;
/** Patient-reported outcomes & experience (module 170).
 *  Approved measurement instruments and their language versions
 *  are managed centrally; measurement timing is fixed to the care
 *  pathway; results are linked to the pathway WITHOUT replacing
 *  clinical assessment. Analysis respects sample size and bias,
 *  urgent-need signals in responses are followed up as clinical
 *  events, and results feed service improvement. */
@RestController @RequestMapping("/api/proms")
public class PromsController {
  private final PromInstrumentRepository instruments; private final PromScheduleRepository schedules;
  private final PromResponseRepository responses; private final PromAnalysisRepository analyses; private final AuditService audit;
  public PromsController(PromInstrumentRepository instruments, PromScheduleRepository schedules, PromResponseRepository responses,
                         PromAnalysisRepository analyses, AuditService audit) {
    this.instruments=instruments; this.schedules=schedules; this.responses=responses; this.analyses=analyses; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record InstrumentRequest(String name, String version, String language, Boolean validated, String domain) {}
  @PostMapping("/instruments")
  Map<String,Object> instrument(@RequestBody InstrumentRequest r) {
    var p = principal();
    var i = instruments.save(new PromInstrument(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.version(), r.language(),
      Boolean.TRUE.equals(r.validated()), r.domain()));
    return Map.of("ok",true,"instrument",i);
  }
  public record ScheduleRequest(String patientRef, UUID instrumentId, String carePathway, String timingPoint, LocalDate dueAt) {}
  @PostMapping("/schedules")
  Map<String,Object> schedule(@RequestBody ScheduleRequest r) {
    var p = principal();
    var i = instruments.findById(r.instrumentId()).orElseThrow(() -> BizException.notFound("instrument_not_found","الأداة غير موجودة"));
    if (!PromRules.instrumentUsable(i.isValidated()))
      throw BizException.conflict("instrument_not_validated","لا يُجدول إلا أدوات قياس معتمدة بنسخ لغوية معتمدة");
    var s = schedules.save(new PromSchedule(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.instrumentId(), r.carePathway(), r.timingPoint(), r.dueAt()));
    audit.record("prom.scheduled","prom_schedule",s.getId().toString(),"{}");
    return Map.of("ok",true,"schedule",s);
  }
  public record ResponseRequest(BigDecimal score, Boolean urgentSignal) {}
  @PostMapping("/schedules/{id}/responses")
  Map<String,Object> respond(@PathVariable UUID id, @RequestBody ResponseRequest r) {
    var p = principal();
    var s = schedules.findById(id).orElseThrow(() -> BizException.notFound("schedule_not_found","الجدولة غير موجودة"));
    var resp = responses.save(new PromResponse(UUID.fromString(p.facilityId()), p.governorate(), id, r.score(), Boolean.TRUE.equals(r.urgentSignal())));
    s.setStatus("responded"); schedules.save(s);
    audit.record("prom.response.received","prom_response",resp.getId().toString(),"{}");
    return Map.of("ok",true,"response",resp,
      "urgent_signal",resp.isUrgentSignal(),
      "note","PROMs complement the clinical assessment — they never replace it" + (resp.isUrgentSignal() ? "; URGENT signal: clinical follow-up required before closure" : ""));
  }
  @PostMapping("/responses/{rid}/urgent-follow-up")
  Map<String,Object> followUp(@PathVariable UUID rid) {
    principal();
    var resp = responses.findById(rid).orElseThrow(() -> BizException.notFound("response_not_found","الرد غير موجود"));
    resp.followUpUrgent(); responses.save(resp);
    audit.record("prom.urgent.followed_up","prom_response",rid.toString(),"{}");
    return Map.of("ok",true,"response",resp);
  }
  @PostMapping("/schedules/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal();
    var s = schedules.findById(id).orElseThrow(() -> BizException.notFound("schedule_not_found","الجدولة غير موجودة"));
    var resp = responses.findByScheduleId(id);
    if (resp != null && !PromRules.scheduleClosable(resp.isUrgentSignal(), resp.isUrgentFollowedUp()))
      throw BizException.conflict("urgent_signal_open","رد يحمل إشارة احتياج عاجل لا تُغلق جدولته قبل متابعة الإشارة سريريًا");
    s.setStatus("closed"); schedules.save(s);
    audit.record("prom.schedule.closed","prom_schedule",id.toString(),"{}");
    return Map.of("ok",true,"schedule",s);
  }
  public record AnalysisRequest(String carePathway, String period, Integer sampleSize, BigDecimal responseRatePct, BigDecimal meanScore, String biasNote) {}
  @PostMapping("/analyses")
  Map<String,Object> analyze(@RequestBody AnalysisRequest r) {
    var p = principal();
    boolean publishable = PromRules.analysisPublishable(r.sampleSize() == null ? 0 : r.sampleSize(),
      r.responseRatePct() == null ? null : r.responseRatePct().doubleValue());
    var a = analyses.save(new PromAnalysis(UUID.fromString(p.facilityId()), p.governorate(), r.carePathway(), r.period(),
      r.sampleSize() == null ? 0 : r.sampleSize(), r.responseRatePct(), r.meanScore(), r.biasNote(), publishable));
    audit.record("prom.analysis.recorded","prom_analysis",a.getId().toString(),"{}");
    return Map.of("ok",true,"analysis",a,"published_as_conclusion",publishable,
      "note",publishable ? "sample and response rate support publication" : "below the minimum publishable sample (30) — figures describe the respondents only, NOT the service");
  }
}
