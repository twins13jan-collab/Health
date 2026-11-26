package eg.gov.health.platform.geodata;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.time.*; import java.util.*;
/** Geographic data & spatial analysis (module 121). Administrative
 *  boundaries and facility locations with documented coordinates,
 *  recorded accuracy and a verification date — stale verifications
 *  are shown as stale. Coverage, referral and mobile-service zones
 *  are managed on top. Access analysis runs on a trusted road
 *  network, and the platform NEVER presents a straight-line
 *  distance as a travel time: an analysis without a road-network
 *  time is returned explicitly incomplete. */
@RestController @RequestMapping("/api/geo-data")
public class GeoDataController {
  private final FacilityLocationRepository locations; private final CoverageZoneRepository zones;
  private final AccessTimeAnalysisRepository analyses; private final AuditService audit;
  public GeoDataController(FacilityLocationRepository locations, CoverageZoneRepository zones,
                           AccessTimeAnalysisRepository analyses, AuditService audit) {
    this.locations=locations; this.zones=zones; this.analyses=analyses; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record LocationRequest(String facilityRef, String name, BigDecimal latitude, BigDecimal longitude,
                                BigDecimal accuracyM, LocalDate verifiedAt, String district) {}
  @PostMapping("/locations")
  Map<String,Object> locate(@RequestBody LocationRequest r) {
    var p = principal();
    if (!GeoRules.coordinatesValid(r.latitude(), r.longitude()))
      throw BizException.badRequest("coordinates_invalid","إحداثيات موثقة داخل الحدود الوطنية مطلوبة");
    if (r.accuracyM() == null || r.verifiedAt() == null)
      throw BizException.badRequest("accuracy_verification","دقة الموقع وتاريخ التحقق جزء من بيانات الموقع");
    var l = locations.save(new FacilityLocation(UUID.fromString(p.facilityId()), p.governorate(), r.facilityRef(), r.name(), r.latitude(),
      r.longitude(), r.accuracyM(), r.verifiedAt(), p.email(), r.district()));
    audit.record("geo.location.recorded","facility_location",l.getId().toString(),"{}");
    return Map.of("ok",true,"location",l,"verification_stale",GeoRules.verificationStale(r.verifiedAt(), LocalDate.now()));
  }
  @GetMapping("/locations/{id}")
  Map<String,Object> getLocation(@PathVariable UUID id) {
    principal();
    var l = locations.findById(id).orElseThrow(() -> BizException.notFound("location_not_found","الموقع غير موجود"));
    return Map.of("ok",true,"location",l,"accuracy_m",String.valueOf(l.getAccuracyM()),
      "verified_at",String.valueOf(l.getVerifiedAt()),"verification_stale",GeoRules.verificationStale(l.getVerifiedAt(), LocalDate.now()));
  }
  public record ZoneRequest(String name, String zoneType, String facilityRef, BigDecimal radiusKm) {}
  @PostMapping("/zones")
  Map<String,Object> zone(@RequestBody ZoneRequest r) {
    var p = principal();
    var z = zones.save(new CoverageZone(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.zoneType(), r.facilityRef(), r.radiusKm()));
    audit.record("geo.zone.created","coverage_zone",z.getId().toString(),"{}");
    return Map.of("ok",true,"zone",z);
  }
  public record AnalysisRequest(String originRef, String facilityRef, BigDecimal straightLineKm, BigDecimal roadDistanceKm,
                                Integer roadTimeMin, String roadNetworkSource) {}
  @PostMapping("/access-analyses")
  Map<String,Object> analyze(@RequestBody AnalysisRequest r) {
    var p = principal();
    var a = analyses.save(new AccessTimeAnalysis(UUID.fromString(p.facilityId()), p.governorate(), r.originRef(), r.facilityRef(),
      r.straightLineKm(), r.roadDistanceKm(), r.roadTimeMin(), r.roadNetworkSource()));
    boolean complete = GeoRules.analysisComplete(r.roadTimeMin(), r.roadNetworkSource());
    audit.record("geo.access.analyzed","access_time_analysis",a.getId().toString(),"{}");
    return Map.of("ok",true,"analysis",a,"analysis_complete",complete,
      "presentation",GeoRules.presentationNote(r.straightLineKm(), r.roadTimeMin()));
  }
}
