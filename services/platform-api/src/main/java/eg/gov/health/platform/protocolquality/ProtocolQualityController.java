package eg.gov.health.platform.protocolquality;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Clinical protocol quality monitoring (module 187). Each
 *  protocol is linked to its scientific source and approving
 *  body; scope, exceptions and contraindications are managed;
 *  review, withdrawal and update dates are managed; adherence
 *  and justified vs unjustified deviations are measured; and
 *  alerts attach to the version in force. */
@RestController @RequestMapping("/api/protocol-quality")
public class ProtocolQualityController {
  private final MonitoredProtocolRepository protocols; private final ProtocolDeviationRepository deviations;
  private final ProtocolAdherenceReportRepository adherence; private final AuditService audit;
  public ProtocolQualityController(MonitoredProtocolRepository protocols, ProtocolDeviationRepository deviations,
                                   ProtocolAdherenceReportRepository adherence, AuditService audit) {
    this.protocols=protocols; this.deviations=deviations; this.adherence=adherence; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private MonitoredProtocol protocol(UUID id) {
    return protocols.findById(id).orElseThrow(() -> BizException.notFound("protocol_not_found","البروتوكول غير موجود"));
  }

  public record ProtocolBody(String name, String sourceRef, String approvingBody, String scope, String contraindications, String version, LocalDate reviewDue) {}
  @PostMapping("/protocols")
  Map<String,Object> create(@RequestBody ProtocolBody r) {
    var p = principal();
    if (r.sourceRef() == null || r.sourceRef().isBlank() || r.approvingBody() == null || r.approvingBody().isBlank())
      throw BizException.badRequest("source_required","بروتوكول بلا مصدر علمي وجهة اعتماد لا يلزم أحدًا");
    var pr = protocols.save(new MonitoredProtocol(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.sourceRef(), r.approvingBody(), r.scope(), r.contraindications(), r.version(), r.reviewDue()));
    return Map.of("ok",true,"protocol",pr);
  }
  @GetMapping("/protocols/{id}/applicability")
  Map<String,Object> applicability(@PathVariable UUID id) {
    principal(); var pr = protocol(id);
    boolean applicable = ProtocolQualityRules.applicable(pr.getStatus(), pr.getReviewDue(), LocalDate.now());
    return Map.of("ok",true,"protocol_id",id.toString(),"status",pr.getStatus(),"applicable",applicable,
      "rule","only a current, in-review-date protocol binds care — withdrawn or stale protocols apply to no one");
  }
  @PostMapping("/protocols/{id}/withdraw")
  Map<String,Object> withdraw(@PathVariable UUID id) {
    principal(); var pr = protocol(id);
    pr.setStatus("withdrawn"); protocols.save(pr);
    audit.record("protocol.withdrawn","monitored_protocol",id.toString(),"{}");
    return Map.of("ok",true,"protocol",pr,"alerts_note","alerts attach to the version in force — this version carries none anymore");
  }
  public record DeviationBody(String deviationType, String reason) {}
  @PostMapping("/protocols/{id}/deviations")
  Map<String,Object> deviate(@PathVariable UUID id, @RequestBody DeviationBody r) {
    var p = principal(); protocol(id);
    String type = r.deviationType();
    if ("justified".equals(type) && (r.reason() == null || r.reason().isBlank())) type = "unjustified";
    var d = deviations.save(new ProtocolDeviation(UUID.fromString(p.facilityId()), p.governorate(), id, type, r.reason()));
    var all = deviations.findByProtocolId(id);
    long unjust = all.stream().filter(x -> "unjustified".equals(x.getDeviationType())).count();
    double rate = ProtocolQualityRules.unjustifiedRatePct(all.size(), unjust);
    return Map.of("ok",true,"deviation",d,"recorded_as",type,"unjustified_rate_pct",rate,
      "quality_review_triggered",rate > ProtocolQualityRules.UNJUSTIFIED_REVIEW_ABOVE_PCT);
  }
  public record AdherenceBody(Integer totalCases, Integer adherentCases) {}
  @PostMapping("/protocols/{id}/adherence")
  Map<String,Object> adhere(@PathVariable UUID id, @RequestBody AdherenceBody r) {
    var p = principal(); protocol(id);
    var rep = adherence.save(new ProtocolAdherenceReport(UUID.fromString(p.facilityId()), p.governorate(), id, r.totalCases(), r.adherentCases()));
    return Map.of("ok",true,"report",rep,"adherence_pct",ProtocolQualityRules.adherencePct(r.totalCases(), r.adherentCases()));
  }
}
