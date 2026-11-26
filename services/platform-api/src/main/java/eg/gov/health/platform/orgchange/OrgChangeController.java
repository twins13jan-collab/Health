package eg.gov.health.platform.orgchange;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.util.*;
/** Organizational change & system adoption (module 162). Change
 *  impact on roles, policies and data is analyzed before anything
 *  moves; communication and training are managed, change champions
 *  carry the message, and user feedback is collected and addressed.
 *  Go-live is gated: impact analysis + communication + 80% training
 *  + updated procedures and delegations. Adoption is declared from
 *  measured actual use and workaround reports — a workaround is
 *  the organization voting with its feet. */
@RestController @RequestMapping("/api/org-change")
public class OrgChangeController {
  private final ChangeInitiativeRepository initiatives; private final ChangeCommunicationRepository comms;
  private final TrainingCompletionRepository training; private final ChangeChampionRepository champions;
  private final UserFeedbackItemRepository feedback; private final AdoptionMetricRepository metrics; private final AuditService audit;
  public OrgChangeController(ChangeInitiativeRepository initiatives, ChangeCommunicationRepository comms, TrainingCompletionRepository training,
                             ChangeChampionRepository champions, UserFeedbackItemRepository feedback, AdoptionMetricRepository metrics, AuditService audit) {
    this.initiatives=initiatives; this.comms=comms; this.training=training; this.champions=champions; this.feedback=feedback; this.metrics=metrics; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ChangeInitiative initiative(UUID id) {
    return initiatives.findById(id).orElseThrow(() -> BizException.notFound("initiative_not_found","مبادرة التغيير غير موجودة"));
  }

  public record InitiativeRequest(String title, String scope, String impactAnalysis, Boolean sopUpdated, Integer affectedUsers) {}
  @PostMapping("/initiatives")
  Map<String,Object> create(@RequestBody InitiativeRequest r) {
    var p = principal();
    var i = initiatives.save(new ChangeInitiative(UUID.fromString(p.facilityId()), p.governorate(), r.title(), r.scope(), r.impactAnalysis(),
      Boolean.TRUE.equals(r.sopUpdated()), r.affectedUsers()));
    audit.record("change.initiative.created","change_initiative",i.getId().toString(),"{}");
    return Map.of("ok",true,"initiative",i);
  }
  public record CommRequest(String audience, String channel) {}
  @PostMapping("/initiatives/{id}/communications")
  Map<String,Object> communicate(@PathVariable UUID id, @RequestBody CommRequest r) {
    var p = principal(); initiative(id);
    var c = comms.save(new ChangeCommunication(UUID.fromString(p.facilityId()), p.governorate(), id, r.audience(), r.channel()));
    return Map.of("ok",true,"communication",c);
  }
  public record TrainingRequest(String userRef, String userRole, Boolean completed) {}
  @PostMapping("/initiatives/{id}/training")
  Map<String,Object> train(@PathVariable UUID id, @RequestBody TrainingRequest r) {
    var p = principal(); initiative(id);
    var t = training.save(new TrainingCompletion(UUID.fromString(p.facilityId()), p.governorate(), id, r.userRef(), r.userRole(), Boolean.TRUE.equals(r.completed())));
    return Map.of("ok",true,"training",t);
  }
  public record ChampionRequest(String champion, String department) {}
  @PostMapping("/initiatives/{id}/champions")
  Map<String,Object> champion(@PathVariable UUID id, @RequestBody ChampionRequest r) {
    var p = principal(); initiative(id);
    var c = champions.save(new ChangeChampion(UUID.fromString(p.facilityId()), p.governorate(), id, r.champion(), r.department()));
    return Map.of("ok",true,"champion",c);
  }
  @PostMapping("/initiatives/{id}/go-live")
  Map<String,Object> goLive(@PathVariable UUID id) {
    principal(); var i = initiative(id);
    long trained = training.countByInitiativeIdAndCompletedTrue(id);
    long affected = i.getAffectedUsers() == null ? 0 : i.getAffectedUsers();
    boolean communicated = comms.existsByInitiativeId(id);
    if (!ChangeRules.goLivePermitted(i.getImpactAnalysis(), communicated, trained, affected, i.isSopUpdated()))
      throw BizException.conflict("go_live_blocked","الإطلاق يتطلب تحليل أثر موثقًا وتواصلًا منجزًا وتدريب 80% من المتأثرين وإجراءات وتفويضات محدثة");
    i.setStatus("live"); initiatives.save(i);
    audit.record("change.went_live","change_initiative",id.toString(),"{}");
    return Map.of("ok",true,"initiative",i,"training_pct",affected == 0 ? 0 : Math.round(100.0 * trained / affected));
  }
  public record FeedbackRequest(String userRef, String sentiment, String comment) {}
  @PostMapping("/initiatives/{id}/feedback")
  Map<String,Object> fb(@PathVariable UUID id, @RequestBody FeedbackRequest r) {
    var p = principal(); initiative(id);
    var f = feedback.save(new UserFeedbackItem(UUID.fromString(p.facilityId()), p.governorate(), id, r.userRef(), r.sentiment(), r.comment()));
    return Map.of("ok",true,"feedback",f);
  }
  public record MetricRequest(BigDecimal activeUsersPct, Integer workaroundReports) {}
  @PostMapping("/initiatives/{id}/adoption-metrics")
  Map<String,Object> metric(@PathVariable UUID id, @RequestBody MetricRequest r) {
    var p = principal(); var i = initiative(id);
    var m = metrics.save(new AdoptionMetric(UUID.fromString(p.facilityId()), p.governorate(), id, r.activeUsersPct(), r.workaroundReports()));
    boolean adopted = ChangeRules.adoptionDeclared(r.activeUsersPct() == null ? null : r.activeUsersPct().doubleValue(),
      r.workaroundReports() == null ? 0 : r.workaroundReports());
    if (adopted && "live".equals(i.getStatus())) { i.setStatus("adopted"); initiatives.save(i); }
    audit.record("change.adoption.measured","adoption_metric",m.getId().toString(),"{}");
    return Map.of("ok",true,"metric",m,"adoption_declared",adopted,
      "note",adopted ? "measured use confirms adoption" : "not adopted yet — usage under 70% or workarounds still reported");
  }
}
