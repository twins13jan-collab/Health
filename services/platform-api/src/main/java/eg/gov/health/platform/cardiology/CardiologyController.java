package eg.gov.health.platform.cardiology;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Cardiology, cath lab & vascular interventions (module 87).
 *  Chest-pain pathways run on documented clocks (arrival, ECG,
 *  troponin, balloon) with the STEMI door-to-balloon target of 90
 *  minutes flagged on breach. Cath bookings carry a preparation
 *  checklist — consent, allergy, renal function, antiplatelet load,
 *  team — and a procedure cannot start unprepared. Findings, stents
 *  and implanted devices are documented with serial/lot traceability
 *  linked to cost and claim; complications (bleeding, AKI, rhythm)
 *  are followed; discharge binds cardiac rehab and secondary
 *  prevention. */
@RestController @RequestMapping("/api/cardiology")
public class CardiologyController {
  private final ChestPainPathwayRepository pathways; private final CathBookingRepository bookings;
  private final CathProcedureRepository procedures; private final CathImplantRepository implants;
  private final CathComplicationRepository complications; private final CardiacRehabEnrollmentRepository rehab;
  private final AuditService audit;
  public CardiologyController(ChestPainPathwayRepository pathways, CathBookingRepository bookings, CathProcedureRepository procedures,
                              CathImplantRepository implants, CathComplicationRepository complications,
                              CardiacRehabEnrollmentRepository rehab, AuditService audit) {
    this.pathways=pathways; this.bookings=bookings; this.procedures=procedures; this.implants=implants;
    this.complications=complications; this.rehab=rehab; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ChestPainPathway pathway(UUID id) {
    return pathways.findById(id).orElseThrow(() -> BizException.notFound("pathway_not_found","مسار ألم الصدر غير موجود"));
  }
  private CathBooking booking(UUID id) {
    return bookings.findById(id).orElseThrow(() -> BizException.notFound("booking_not_found","حجز القسطرة غير موجود"));
  }

  public record PathwayRequest(String patientRef, OffsetDateTime arrivalAt) {}
  @PostMapping("/pathways")
  Map<String,Object> pathway(@RequestBody PathwayRequest r) {
    var p = principal();
    if (r.arrivalAt()==null) throw BizException.badRequest("arrival_required","مسار ألم الصدر يبدأ بساعة وصول موثقة");
    var pw = pathways.save(new ChestPainPathway(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.arrivalAt()));
    audit.record("cardiology.pathway.opened","chest_pain_pathway",pw.getId().toString(),"{}");
    return Map.of("ok",true,"pathway",pw);
  }
  public record EcgRequest(OffsetDateTime ecgAt, String finding) {}
  @PostMapping("/pathways/{id}/ecg")
  Map<String,Object> ecg(@PathVariable UUID id, @RequestBody EcgRequest r) {
    var pw = pathway(id); pw.recordEcg(r.ecgAt(), r.finding()); pathways.save(pw);
    audit.record("cardiology.ecg.recorded","chest_pain_pathway",id.toString(),"{}");
    long doorToEcg = (r.ecgAt()!=null) ? Duration.between(pw.getArrivalAt(), r.ecgAt()).toMinutes() : -1;
    return Map.of("ok",true,"pathway",pw,"door_to_ecg_minutes",doorToEcg);
  }
  public record TroponinRequest(String result, OffsetDateTime at) {}
  @PostMapping("/pathways/{id}/troponin")
  Map<String,Object> troponin(@PathVariable UUID id, @RequestBody TroponinRequest r) {
    var pw = pathway(id); pw.recordTroponin(r.result(), r.at()); pathways.save(pw);
    audit.record("cardiology.troponin.recorded","chest_pain_pathway",id.toString(),"{}");
    return Map.of("ok",true,"pathway",pw);
  }
  @PostMapping("/pathways/{id}/pathway-decision")
  Map<String,Object> decide(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var pw = pathway(id);
    if (!Set.of("primary_pci","thrombolysis","medical").contains(body.get("pathway")))
      throw BizException.badRequest("pathway_choice","المسار: قسطرة أولية أو إذابة أو علاج دوائي");
    pw.setPathway(body.get("pathway")); pathways.save(pw);
    audit.record("cardiology.pathway.decided","chest_pain_pathway",id.toString(),"{}");
    return Map.of("ok",true,"pathway",pw);
  }
  @PostMapping("/pathways/{id}/balloon")
  Map<String,Object> balloon(@PathVariable UUID id, @RequestBody Map<String,OffsetDateTime> body) {
    var pw = pathway(id);
    OffsetDateTime at = body.get("balloonAt");
    if (at==null) throw BizException.badRequest("balloon_time","وقت فتح الشريان (البالون) مطلوب");
    pw.recordBalloon(at); pathways.save(pw);
    long d2b = CardiologyRules.doorToBalloonMinutes(pw.getArrivalAt(), at);
    boolean breached = CardiologyRules.doorToBalloonBreached(pw.getArrivalAt(), at);
    audit.record("cardiology.balloon.done","chest_pain_pathway",id.toString(),"{}");
    return Map.of("ok",true,"pathway",pw,"door_to_balloon_minutes",d2b,"target_minutes",90,"target_breached",breached);
  }
  public record BookingRequest(UUID pathwayId, String patientRef, String lab, OffsetDateTime scheduledAt) {}
  @PostMapping("/bookings")
  Map<String,Object> book(@RequestBody BookingRequest r) {
    var p = principal();
    var b = bookings.save(new CathBooking(UUID.fromString(p.facilityId()), p.governorate(), r.pathwayId(), r.patientRef(), r.lab(), r.scheduledAt()));
    audit.record("cardiology.cath.booked","cath_booking",b.getId().toString(),"{}");
    return Map.of("ok",true,"booking",b);
  }
  public record PrepareRequest(Boolean consent, Boolean allergyChecked, String renalFunction, Boolean antiplateletLoaded, Boolean teamConfirmed) {}
  @PostMapping("/bookings/{id}/prepare")
  Map<String,Object> prepare(@PathVariable UUID id, @RequestBody PrepareRequest r) {
    var b = booking(id);
    b.prepare(Boolean.TRUE.equals(r.consent()), Boolean.TRUE.equals(r.allergyChecked()), r.renalFunction(),
      Boolean.TRUE.equals(r.antiplateletLoaded()), Boolean.TRUE.equals(r.teamConfirmed()));
    bookings.save(b);
    audit.record("cardiology.cath.prepared","cath_booking",id.toString(),"{}");
    return Map.of("ok",true,"booking",b,"preparation_complete","prepared".equals(b.getStatus()));
  }
  public record ProcedureRequest(String findings, String operator, Integer contrastVolumeMl) {}
  @PostMapping("/bookings/{id}/procedure")
  Map<String,Object> procedure(@PathVariable UUID id, @RequestBody ProcedureRequest r) {
    var p = principal(); var b = booking(id);
    if (!"prepared".equals(b.getStatus()))
      throw BizException.conflict("not_prepared","لا تبدأ القسطرة قبل اكتمال تجهيز المريض والفريق والأجهزة والموافقات");
    var proc = procedures.save(new CathProcedure(b.getFacilityId(), p.governorate(), id, r.findings(), r.operator(), r.contrastVolumeMl()));
    b.setStatus("done"); bookings.save(b);
    boolean aki = CardiologyRules.akiWatch(r.contrastVolumeMl(), b.getRenalFunction());
    audit.record("cardiology.cath.done","cath_procedure",proc.getId().toString(),"{}");
    return Map.of("ok",true,"procedure",proc,"aki_watch",aki,
      "note", aki ? "high contrast with impaired renal function — AKI monitoring required" : "standard post-cath monitoring");
  }
  public record ImplantRequest(String itemType, String description, String serialLot, String costRef, String claimRef) {}
  @PostMapping("/procedures/{id}/implants")
  Map<String,Object> implant(@PathVariable UUID id, @RequestBody ImplantRequest r) {
    var p = principal();
    var proc = procedures.findById(id).orElseThrow(() -> BizException.notFound("procedure_not_found","إجراء القسطرة غير موجود"));
    // High-value supplies are traceable or they are not recorded at all.
    if (!CardiologyRules.implantTraceable(r.serialLot()))
      throw BizException.badRequest("serial_required","المستلزمات عالية القيمة تربط بالمخزون والتكلفة والمطالبات برقم تسلسلي/تشغيلة");
    var im = implants.save(new CathImplant(procFac(proc), p.governorate(), id, r.itemType(), r.description(), r.serialLot(), r.costRef(), r.claimRef()));
    audit.record("cardiology.implant.recorded","cath_implant",im.getId().toString(),"{}");
    return Map.of("ok",true,"implant",im);
  }
  private UUID procFac(CathProcedure pr) { return pr.getFacilityId(); }
  public record ComplicationRequest(String kind, String severity, String management) {}
  @PostMapping("/procedures/{id}/complications")
  Map<String,Object> complication(@PathVariable UUID id, @RequestBody ComplicationRequest r) {
    var p = principal();
    var proc = procedures.findById(id).orElseThrow(() -> BizException.notFound("procedure_not_found","إجراء القسطرة غير موجود"));
    var c = complications.save(new CathComplication(procFac(proc), p.governorate(), id, r.kind(), r.severity(), r.management()));
    audit.record("cardiology.complication.recorded","cath_complication",c.getId().toString(),"{}");
    return Map.of("ok",true,"complication",c);
  }
  public record RehabRequest(String patientRef, String program, Boolean statin, Boolean antiplatelet, Boolean betaBlocker, Boolean smokingCessation) {}
  @PostMapping("/rehab")
  Map<String,Object> rehab(@RequestBody RehabRequest r) {
    var p = principal();
    var e = rehab.save(new CardiacRehabEnrollment(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.program(),
      Boolean.TRUE.equals(r.statin()), Boolean.TRUE.equals(r.antiplatelet()), Boolean.TRUE.equals(r.betaBlocker()), Boolean.TRUE.equals(r.smokingCessation())));
    audit.record("cardiology.rehab.enrolled","cardiac_rehab",e.getId().toString(),"{}");
    return Map.of("ok",true,"enrollment",e,"note","cardiac rehabilitation and secondary prevention bound at discharge");
  }
}
