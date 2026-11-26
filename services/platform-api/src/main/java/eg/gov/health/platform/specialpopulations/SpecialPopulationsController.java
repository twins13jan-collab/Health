package eg.gov.health.platform.specialpopulations;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Health services for special populations & sites (module 70).
 *  Services in schools, universities, youth centres and sports
 *  facilities; healthcare in detention under legal safeguards
 *  and patient rights; services for refugees, displaced and
 *  mobile populations without unjustified barriers; service
 *  points at ports, airports and quarantine; and medical cover
 *  for missions, events, mass gatherings and seasons. */
@RestController @RequestMapping("/api/special-populations")
public class SpecialPopulationsController {
  private final SpecialServicePointRepository points; private final DetentionCareRecordRepository detention;
  private final MobilePopulationEncounterRepository encounters; private final AuditService audit;
  public SpecialPopulationsController(SpecialServicePointRepository points, DetentionCareRecordRepository detention,
                                      MobilePopulationEncounterRepository encounters, AuditService audit) {
    this.points=points; this.detention=detention; this.encounters=encounters; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private SpecialServicePoint point(UUID id) {
    return points.findById(id).orElseThrow(() -> BizException.notFound("point_not_found","نقطة الخدمة غير موجودة"));
  }

  public record PointBody(String siteType, String name, Boolean safeguardsVerified) {}
  @PostMapping("/points")
  Map<String,Object> create(@RequestBody PointBody r) {
    var p = principal();
    var pt = points.save(new SpecialServicePoint(UUID.fromString(p.facilityId()), p.governorate(), r.siteType(), r.name(), Boolean.TRUE.equals(r.safeguardsVerified())));
    return Map.of("ok",true,"point",pt);
  }
  @PostMapping("/points/{id}/activate")
  Map<String,Object> activate(@PathVariable UUID id) {
    principal(); var pt = point(id);
    if (!SpecialPopRules.pointActivatable(pt.getSiteType(), pt.isSafeguardsVerified()))
      throw BizException.conflict("safeguards_unverified","نقطة خدمة احتجاز لا تُفعَّل قبل التحقق من الضمانات القانونية وحقوق المرضى");
    pt.activate(); points.save(pt);
    audit.record("special.point.activated","special_service_point",id.toString(),"{}");
    return Map.of("ok",true,"point",pt);
  }
  public record DetentionBody(String patientRef, Boolean legalSafeguardsConfirmed, Boolean rightsInformed, String careSummary) {}
  @PostMapping("/points/{id}/detention-care")
  Map<String,Object> detentionCare(@PathVariable UUID id, @RequestBody DetentionBody r) {
    var p = principal(); point(id);
    var rec = detention.save(new DetentionCareRecord(UUID.fromString(p.facilityId()), p.governorate(), id, r.patientRef(),
      Boolean.TRUE.equals(r.legalSafeguardsConfirmed()), Boolean.TRUE.equals(r.rightsInformed()), r.careSummary()));
    audit.record("special.detention.care","detention_care_record",rec.getId().toString(),"{}");
    return Map.of("ok",true,"record",rec);
  }
  @PostMapping("/detention-care/{rid}/close")
  Map<String,Object> closeDetention(@PathVariable UUID rid) {
    principal();
    var rec = detention.findById(rid).orElseThrow(() -> BizException.notFound("record_not_found","السجل غير موجود"));
    if (!SpecialPopRules.detentionRecordClosable(rec.isLegalSafeguardsConfirmed(), rec.isRightsInformed()))
      throw BizException.conflict("rights_unconfirmed","رعاية في احتجاز لا يُغلق سجلها قبل تأكيد الضمانات القانونية وإعلام المريض بحقوقه — الاحتجاز يغير المكان لا المعيار");
    rec.close(); detention.save(rec);
    return Map.of("ok",true,"record",rec);
  }
  public record EncounterBody(String personRef, String barrierFlag, Boolean careProvided) {}
  @PostMapping("/points/{id}/encounters")
  Map<String,Object> encounter(@PathVariable UUID id, @RequestBody EncounterBody r) {
    var p = principal(); point(id);
    boolean review = SpecialPopRules.denialReviewRequired(r.barrierFlag(), Boolean.TRUE.equals(r.careProvided()));
    var e = encounters.save(new MobilePopulationEncounter(UUID.fromString(p.facilityId()), p.governorate(), id, r.personRef(),
      r.barrierFlag(), Boolean.TRUE.equals(r.careProvided()), review));
    if (review) audit.record("special.denial.review_flagged","mobile_population_encounter",e.getId().toString(),"{}");
    return Map.of("ok",true,"encounter",e,"denial_review_required",review,
      "note",review ? "care withheld over documents/fees is an unjustified barrier — flagged for review" : "recorded");
  }
}
