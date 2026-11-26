package eg.gov.health.platform.hazmat;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Environmental, chemical & radiological emergencies
 *  (module 173). Agents and exposure scope are classified;
 *  isolation, decontamination and triage are managed; PPE and
 *  specialist teams are coordinated — with poison centres and
 *  the competent authorities — and exposure, examinations and
 *  follow-up are documented per person. */
@RestController @RequestMapping("/api/hazmat")
public class HazmatController {
  private final HazmatIncidentRepository incidents; private final ExposureRecordRepository exposures;
  private final SpecialistCoordinationRepository coordination; private final AuditService audit;
  public HazmatController(HazmatIncidentRepository incidents, ExposureRecordRepository exposures,
                          SpecialistCoordinationRepository coordination, AuditService audit) {
    this.incidents=incidents; this.exposures=exposures; this.coordination=coordination; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private HazmatIncident incident(UUID id) {
    return incidents.findById(id).orElseThrow(() -> BizException.notFound("incident_not_found","الحادث غير موجود"));
  }

  public record IncidentRequest(String agentType, String agentName, String exposureScope) {}
  @PostMapping("/incidents")
  Map<String,Object> declare(@RequestBody IncidentRequest r) {
    var p = principal();
    var i = incidents.save(new HazmatIncident(UUID.fromString(p.facilityId()), p.governorate(), r.agentType(), r.agentName(), r.exposureScope()));
    audit.record("hazmat.incident.declared","hazmat_incident",i.getId().toString(),"{}");
    return Map.of("ok",true,"incident",i,"specialist_coordination_required",HazmatRules.specialistCoordinationRequired(r.agentType()));
  }
  @PostMapping("/incidents/{id}/zones")
  Map<String,Object> zones(@PathVariable UUID id) {
    principal(); var i = incident(id);
    i.establishZones(); incidents.save(i);
    return Map.of("ok",true,"incident",i,"zones","hot / warm / cold established — contamination is contained before it is treated");
  }
  public record ExposureRequest(String personRef, String exposureLevel, Boolean contaminated, Boolean followUpRequired) {}
  @PostMapping("/incidents/{id}/exposures")
  Map<String,Object> exposure(@PathVariable UUID id, @RequestBody ExposureRequest r) {
    var p = principal(); incident(id);
    var e = exposures.save(new ExposureRecord(UUID.fromString(p.facilityId()), p.governorate(), id, r.personRef(), r.exposureLevel(),
      Boolean.TRUE.equals(r.contaminated()), Boolean.TRUE.equals(r.followUpRequired())));
    audit.record("hazmat.exposure.recorded","exposure_record",e.getId().toString(),"{}");
    return Map.of("ok",true,"exposure",e);
  }
  @PostMapping("/exposures/{eid}/decontaminate")
  Map<String,Object> decontaminate(@PathVariable UUID eid) {
    principal();
    var e = exposures.findById(eid).orElseThrow(() -> BizException.notFound("exposure_not_found","سجل التعرض غير موجود"));
    e.decontaminate(); exposures.save(e);
    return Map.of("ok",true,"exposure",e);
  }
  @PostMapping("/exposures/{eid}/enter-treatment")
  Map<String,Object> enterTreatment(@PathVariable UUID eid) {
    principal();
    var e = exposures.findById(eid).orElseThrow(() -> BizException.notFound("exposure_not_found","سجل التعرض غير موجود"));
    if (!HazmatRules.treatmentEntryPermitted(e.isContaminated(), e.isDecontaminated()))
      throw BizException.conflict("still_contaminated","مصاب ملوث لا يدخل منطقة العلاج قبل التطهير — مريض واحد غير مطهّر قد يغلق قسم طوارئ كاملًا");
    e.enterTreatment(); exposures.save(e);
    audit.record("hazmat.casualty.in_treatment","exposure_record",eid.toString(),"{}");
    return Map.of("ok",true,"exposure",e);
  }
  @PostMapping("/exposures/{eid}/follow-up-done")
  Map<String,Object> followUp(@PathVariable UUID eid) {
    principal();
    var e = exposures.findById(eid).orElseThrow(() -> BizException.notFound("exposure_not_found","سجل التعرض غير موجود"));
    e.completeFollowUp(); exposures.save(e);
    return Map.of("ok",true,"exposure",e);
  }
  public record CoordRequest(String coordinatedWith) {}
  @PostMapping("/incidents/{id}/coordinate")
  Map<String,Object> coordinate(@PathVariable UUID id, @RequestBody CoordRequest r) {
    var p = principal(); incident(id);
    var c = coordination.save(new SpecialistCoordination(UUID.fromString(p.facilityId()), p.governorate(), id, r.coordinatedWith()));
    audit.record("hazmat.coordinated","specialist_coordination",c.getId().toString(),"{}");
    return Map.of("ok",true,"coordination",c);
  }
  @PostMapping("/incidents/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal(); var i = incident(id);
    long owing = exposures.countByIncidentIdAndFollowUpRequiredAndFollowUpDone(id, true, false);
    boolean coordDone = coordination.existsByIncidentId(id);
    if (!HazmatRules.incidentClosable(i.isZonesEstablished(), HazmatRules.specialistCoordinationRequired(i.getAgentType()), coordDone, owing))
      throw BizException.conflict("closure_blocked","لا إغلاق قبل إنشاء المناطق والتنسيق المتخصص الواجب وإتمام فحوص متابعة كل معرّض");
    i.close(); incidents.save(i);
    return Map.of("ok",true,"incident",i);
  }
}
