package eg.gov.health.platform.infectioncontrol;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Infectious diseases, isolation & infection control (module 91).
 *  Suspected and confirmed cases are defined and reported;
 *  notifiable confirmed cases must carry a notification. Isolation
 *  precautions bind PPE and location — airborne means
 *  negative-pressure — and only infection-control officers
 *  discontinue them. Contacts are traced within authority and
 *  followed to a result; outbreaks open epidemiological
 *  investigations; HAI indicators are computed per 1,000 device-days
 *  from raw counts. Antimicrobial stewardship ties prescriptions to
 *  culture results: with a narrower option proven, plain
 *  continuation is an owned stewardship decision. */
@RestController @RequestMapping("/api/infection-control")
public class InfectionControlController {
  private final InfectionCaseRepository cases; private final IsolationOrderRepository isolations;
  private final ContactTracingRepository contacts; private final OutbreakInvestigationRepository outbreaks;
  private final HaiSurveillanceRepository hai; private final AntimicrobialReviewRepository reviews;
  private final AuditService audit;
  public InfectionControlController(InfectionCaseRepository cases, IsolationOrderRepository isolations, ContactTracingRepository contacts,
                                    OutbreakInvestigationRepository outbreaks, HaiSurveillanceRepository hai,
                                    AntimicrobialReviewRepository reviews, AuditService audit) {
    this.cases=cases; this.isolations=isolations; this.contacts=contacts; this.outbreaks=outbreaks;
    this.hai=hai; this.reviews=reviews; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private InfectionCase icase(UUID id) {
    return cases.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","حالة العدوى غير موجودة"));
  }

  public record CaseRequest(String patientRef, String organism, String caseType, String source, String haiType, String unit, LocalDate onsetDate, Boolean notifiable) {}
  @PostMapping("/cases")
  Map<String,Object> open(@RequestBody CaseRequest r) {
    var p = principal();
    var c = cases.save(new InfectionCase(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.organism(), r.caseType(),
      r.source(), r.haiType(), r.unit(), r.onsetDate(), Boolean.TRUE.equals(r.notifiable())));
    audit.record("ipc.case.opened","infection_case",c.getId().toString(),"{}");
    return Map.of("ok",true,"case",c,"notification_breach",InfectionRules.notificationBreach(c.isNotifiable(), c.getCaseType(), c.getNotifiedAt()!=null));
  }
  @PostMapping("/cases/{id}/confirm")
  Map<String,Object> confirm(@PathVariable UUID id) {
    var c = icase(id); c.confirm(); cases.save(c);
    audit.record("ipc.case.confirmed","infection_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c,"notification_breach",InfectionRules.notificationBreach(c.isNotifiable(), c.getCaseType(), c.getNotifiedAt()!=null));
  }
  @PostMapping("/cases/{id}/notify")
  Map<String,Object> notifyCase(@PathVariable UUID id) {
    var c = icase(id); c.notifyNow(); cases.save(c);
    audit.record("ipc.case.notified","infection_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c,"notification_breach",false);
  }
  public record IsolationRequest(UUID caseId, String patientRef, String precautionType, String location, String locationType, String ppeRequired) {}
  @PostMapping("/isolations")
  Map<String,Object> isolate(@RequestBody IsolationRequest r) {
    var p = principal();
    // Airborne precautions in a non-negative-pressure location are not isolation.
    if (!InfectionRules.locationValid(r.precautionType(), r.locationType()))
      throw BizException.badRequest("location_invalid","احتياطات الهواء تتطلب موقع ضغط سلبي موثقًا، وكل عزل يتطلب نوع موقع");
    var o = isolations.save(new IsolationOrder(UUID.fromString(p.facilityId()), p.governorate(), r.caseId(), r.patientRef(),
      r.precautionType(), r.location(), r.locationType(), r.ppeRequired(), p.email()));
    if (r.caseId()!=null) { var c = icase(r.caseId()); c.setStatus("isolated"); cases.save(c); }
    audit.record("ipc.isolation.started","isolation_order",o.getId().toString(),"{}");
    return Map.of("ok",true,"isolation",o);
  }
  @PostMapping("/isolations/{id}/discontinue")
  Map<String,Object> discontinue(@PathVariable UUID id) {
    var p = principal();
    if (!InfectionRules.mayDiscontinueIsolation(p.role()))
      throw BizException.forbidden("إنهاء العزل قرار فريق مكافحة العدوى فقط");
    var o = isolations.findById(id).orElseThrow(() -> BizException.notFound("isolation_not_found","أمر العزل غير موجود"));
    o.discontinue(p.email()); isolations.save(o);
    audit.record("ipc.isolation.discontinued","isolation_order",id.toString(),"{}");
    return Map.of("ok",true,"isolation",o);
  }
  public record ContactRequest(String contactRef, String exposureType, String riskLevel) {}
  @PostMapping("/cases/{id}/contacts")
  Map<String,Object> contact(@PathVariable UUID id, @RequestBody ContactRequest r) {
    var p = principal(); var c = icase(id);
    var t = contacts.save(new ContactTracing(c.getFacilityId(), p.governorate(), id, r.contactRef(), r.exposureType(), r.riskLevel()));
    audit.record("ipc.contact.traced","contact_tracing",t.getId().toString(),"{}");
    return Map.of("ok",true,"contact",t,"note","contacts traced within authority and followed to a result");
  }
  @PostMapping("/contacts/{id}/follow-up")
  Map<String,Object> followUp(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var t = contacts.findById(id).orElseThrow(() -> BizException.notFound("contact_not_found","المخالط غير موجود"));
    t.followUp(body.getOrDefault("status","monitoring"), body.get("testResult")); contacts.save(t);
    audit.record("ipc.contact.followed","contact_tracing",id.toString(),"{}");
    return Map.of("ok",true,"contact",t);
  }
  public record OutbreakRequest(String title, String unit, String suspectedOrganism, Integer casesCount, String controlMeasures) {}
  @PostMapping("/outbreaks")
  Map<String,Object> outbreak(@RequestBody OutbreakRequest r) {
    var p = principal();
    var o = outbreaks.save(new OutbreakInvestigation(UUID.fromString(p.facilityId()), p.governorate(), r.title(), r.unit(),
      r.suspectedOrganism(), r.casesCount()==null?0:r.casesCount(), r.controlMeasures(), p.email()));
    audit.record("ipc.outbreak.opened","outbreak_investigation",o.getId().toString(),"{}");
    return Map.of("ok",true,"outbreak",o);
  }
  public record HaiRequest(String unit, String periodKey, String haiType, Integer deviceDays, Integer infectionsCount) {}
  @PostMapping("/hai-surveillance")
  Map<String,Object> hai(@RequestBody HaiRequest r) {
    var p = principal();
    int dd = r.deviceDays()==null?0:r.deviceDays(); int inf = r.infectionsCount()==null?0:r.infectionsCount();
    var h = hai.save(new HaiSurveillance(UUID.fromString(p.facilityId()), p.governorate(), r.unit(), r.periodKey(), r.haiType(), dd, inf));
    audit.record("ipc.hai.recorded","hai_surveillance",h.getId().toString(),"{}");
    // The indicator is computed from raw counts — never typed in.
    return Map.of("ok",true,"surveillance",h,"rate_per_1000_device_days",InfectionRules.haiRatePer1000(inf, dd));
  }
  public record ReviewRequest(String patientRef, String antibiotic, String indication, String cultureResult, Boolean narrowerOption, String decision) {}
  @PostMapping("/antimicrobial-reviews")
  Map<String,Object> review(@RequestBody ReviewRequest r) {
    var p = principal();
    if (!Set.of("continue","de_escalate","stop","change").contains(r.decision()))
      throw BizException.badRequest("decision_choice","قرار الترشيد: استمرار أو تخفيض طيف أو إيقاف أو تغيير");
    var rv = reviews.save(new AntimicrobialReview(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.antibiotic(),
      r.indication(), r.cultureResult(), Boolean.TRUE.equals(r.narrowerOption()), r.decision(), p.email()));
    audit.record("ipc.antimicrobial.reviewed","antimicrobial_review",rv.getId().toString(),"{}");
    return Map.of("ok",true,"review",rv,
      "stewardship_note", InfectionRules.stewardshipDecisionRequired(r.cultureResult(), Boolean.TRUE.equals(r.narrowerOption()), r.decision())
        ? "continuation despite a culture-proven narrower option — owned stewardship decision, flagged for the AMS committee"
        : "stewardship review recorded against the culture result");
  }
}
