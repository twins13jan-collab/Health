package eg.gov.health.platform.porthealth;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.time.*; import java.util.*;
/** Port health & quarantine (module 145). Traveler health reports
 *  with risk assessment, screening, isolation and referral at
 *  ports of entry. A high-risk symptomatic traveler is isolated or
 *  referred — never cleared at the desk. Quarantine orders run on
 *  a legal basis; early release needs an authorized releaser and a
 *  documented reason. Flight and traveler data is held at the
 *  minimum (a code and a travel reference), coordination with port
 *  authorities is recorded, and referred cases feed the emergency
 *  and medical-evacuation plans. */
@RestController @RequestMapping("/api/port-health")
public class PortHealthController {
  private final TravelerHealthReportRepository reports; private final PortScreeningRepository screenings;
  private final QuarantineOrderRepository orders; private final PortCoordinationNoteRepository notes; private final AuditService audit;
  public PortHealthController(TravelerHealthReportRepository reports, PortScreeningRepository screenings,
                              QuarantineOrderRepository orders, PortCoordinationNoteRepository notes, AuditService audit) {
    this.reports=reports; this.screenings=screenings; this.orders=orders; this.notes=notes; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private TravelerHealthReport report(UUID id) {
    return reports.findById(id).orElseThrow(() -> BizException.notFound("report_not_found","البلاغ غير موجود"));
  }

  public record ReportRequest(String travelerCode, String travelRef, String originCountry, String symptoms, String riskLevel) {}
  @PostMapping("/reports")
  Map<String,Object> report(@RequestBody ReportRequest r) {
    var p = principal();
    var rep = reports.save(new TravelerHealthReport(UUID.fromString(p.facilityId()), p.governorate(), r.travelerCode(), r.travelRef(),
      r.originCountry(), r.symptoms(), r.riskLevel()));
    audit.record("port.report.received","traveler_health_report",rep.getId().toString(),"{}");
    return Map.of("ok",true,"report",rep,"data_minimization","traveler code + travel reference only — no identity documents held in this module");
  }
  public record ScreeningRequest(BigDecimal temperatureC, String screeningResult) {}
  @PostMapping("/reports/{id}/screening")
  Map<String,Object> screen(@PathVariable UUID id, @RequestBody ScreeningRequest r) {
    var p = principal(); var rep = report(id);
    if (!PortHealthRules.screeningPermitted(rep.getRiskLevel(), r.screeningResult()))
      throw BizException.conflict("clearance_blocked","مسافر عالي الخطورة بأعراض يُعزل أو يُحال — لا يُخلى سبيله عند مكتب المنفذ");
    var s = screenings.save(new PortScreening(UUID.fromString(p.facilityId()), p.governorate(), id, r.temperatureC(), r.screeningResult()));
    audit.record("port.screening.done","port_screening",s.getId().toString(),"{}");
    return Map.of("ok",true,"screening",s);
  }
  public record OrderRequest(String facilityRef, String legalBasis, OffsetDateTime startsAt, OffsetDateTime endsAt) {}
  @PostMapping("/reports/{id}/quarantine")
  Map<String,Object> quarantine(@PathVariable UUID id, @RequestBody OrderRequest r) {
    var p = principal(); report(id);
    if (r.legalBasis() == null || r.endsAt() == null || r.startsAt() == null || !r.endsAt().isAfter(r.startsAt()))
      throw BizException.badRequest("quarantine_basis","أمر الحجر يتطلب أساسًا قانونيًا ومدة صالحة");
    var o = orders.save(new QuarantineOrder(UUID.fromString(p.facilityId()), p.governorate(), id, r.facilityRef(), r.legalBasis(), r.startsAt(), r.endsAt()));
    audit.record("port.quarantine.ordered","quarantine_order",o.getId().toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  public record ReleaseRequest(String releaseReason) {}
  @PostMapping("/quarantine/{oid}/release")
  Map<String,Object> release(@PathVariable UUID oid, @RequestBody ReleaseRequest r) {
    var p = principal();
    var o = orders.findById(oid).orElseThrow(() -> BizException.notFound("order_not_found","أمر الحجر غير موجود"));
    var now = OffsetDateTime.now();
    if (PortHealthRules.quarantineComplete(now, o.getEndsAt())) {
      o.complete(); orders.save(o);
      return Map.of("ok",true,"order",o,"outcome","quarantine completed its legal course");
    }
    if (!PortHealthRules.earlyReleasePermitted(now, o.getEndsAt(), p.email(), r.releaseReason()))
      throw BizException.conflict("early_release_blocked","الإفراج المبكر يتطلب مخلِّصًا مخولًا وسببًا موثقًا");
    o.releaseEarly(p.email(), r.releaseReason()); orders.save(o);
    audit.record("port.quarantine.released_early","quarantine_order",oid.toString(),"{}");
    return Map.of("ok",true,"order",o,"outcome","released early by " + p.email() + " — reason on record");
  }
  public record NoteRequest(String authority, String note) {}
  @PostMapping("/reports/{id}/coordination")
  Map<String,Object> coordinate(@PathVariable UUID id, @RequestBody NoteRequest r) {
    var p = principal(); report(id);
    var n = notes.save(new PortCoordinationNote(UUID.fromString(p.facilityId()), p.governorate(), id, r.authority(), r.note()));
    audit.record("port.coordination.noted","port_coordination_note",n.getId().toString(),"{}");
    return Map.of("ok",true,"note",n);
  }
}
