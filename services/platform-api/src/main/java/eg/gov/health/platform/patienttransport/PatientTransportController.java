package eg.gov.health.platform.patienttransport;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Non-emergency patient transport (module 154). Transport is
 *  requested for dialysis, therapy and rehabilitation; wheelchair,
 *  stretcher and oxygen needs are specified; trips and routes are
 *  scheduled with VERIFIED providers; and completion, delay and
 *  complaints are managed per trip. */
@RestController @RequestMapping("/api/patient-transport")
public class PatientTransportController {
  private final TransportProviderRepository providers; private final PatientTransportRequestRepository requests;
  private final TransportTripRepository trips; private final AuditService audit;
  public PatientTransportController(TransportProviderRepository providers, PatientTransportRequestRepository requests,
                                    TransportTripRepository trips, AuditService audit) {
    this.providers=providers; this.requests=requests; this.trips=trips; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record ProviderRequest(String name, Boolean verified, LocalDate licenceValidUntil, Boolean oxygenCapable, Boolean stretcherCapable) {}
  @PostMapping("/providers")
  Map<String,Object> provider(@RequestBody ProviderRequest r) {
    var p = principal();
    var pr = providers.save(new TransportProvider(UUID.fromString(p.facilityId()), p.governorate(), r.name(), Boolean.TRUE.equals(r.verified()),
      r.licenceValidUntil(), Boolean.TRUE.equals(r.oxygenCapable()), Boolean.TRUE.equals(r.stretcherCapable())));
    return Map.of("ok",true,"provider",pr);
  }
  public record TransportRequestBody(String patientRef, String purpose, String needType, Boolean oxygenNeeded, OffsetDateTime scheduledAt) {}
  @PostMapping("/requests")
  Map<String,Object> request(@RequestBody TransportRequestBody r) {
    var p = principal();
    var rq = requests.save(new PatientTransportRequest(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.purpose(), r.needType(),
      Boolean.TRUE.equals(r.oxygenNeeded()), r.scheduledAt()));
    audit.record("transport.requested","transport_request",rq.getId().toString(),"{}");
    return Map.of("ok",true,"request",rq);
  }
  public record ScheduleRequest(UUID providerId) {}
  @PostMapping("/requests/{id}/schedule")
  Map<String,Object> schedule(@PathVariable UUID id, @RequestBody ScheduleRequest r) {
    principal();
    var rq = requests.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    var pr = providers.findById(r.providerId()).orElseThrow(() -> BizException.notFound("provider_not_found","المزود غير موجود"));
    LocalDate tripDate = rq.getScheduledAt() == null ? null : rq.getScheduledAt().toLocalDate();
    if (!TransportRules.providerUsable(pr.isVerified(), pr.getLicenceValidUntil(), tripDate,
        rq.isOxygenNeeded(), pr.isOxygenCapable(), rq.getNeedType(), pr.isStretcherCapable()))
      throw BizException.conflict("provider_unusable","الحجز بمزود موثّق الترخيص ساريًا يوم الرحلة وقادر على الحاجة (كرسي/نقالة/أكسجين) فقط");
    rq.schedule(pr.getId()); requests.save(rq);
    audit.record("transport.scheduled","transport_request",id.toString(),"{}");
    return Map.of("ok",true,"request",rq);
  }
  public record TripRequest(OffsetDateTime departedAt, OffsetDateTime arrivedAt, Integer delayMinutes, Boolean completed, String complaint) {}
  @PostMapping("/requests/{id}/trip")
  Map<String,Object> trip(@PathVariable UUID id, @RequestBody TripRequest r) {
    var p = principal();
    var rq = requests.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    if (!"scheduled".equals(rq.getStatus())) throw BizException.conflict("not_scheduled","رحلة على طلب مجدول");
    var t = trips.save(new TransportTrip(UUID.fromString(p.facilityId()), p.governorate(), id, r.departedAt(), r.arrivedAt(),
      r.delayMinutes(), Boolean.TRUE.equals(r.completed()), r.complaint()));
    if (Boolean.TRUE.equals(r.completed())) { rq.complete(); requests.save(rq); }
    audit.record("transport.trip.recorded","transport_trip",t.getId().toString(),"{}");
    return Map.of("ok",true,"trip",t,"delay_review",TransportRules.delayNeedsReview(r.delayMinutes())
      ? "delay over 30 minutes is recorded for provider review — a pattern, not an anecdote" : "within tolerance");
  }
}
