package eg.gov.health.platform.clinicaltrials;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Clinical trials & medical innovation (module 130). A trial
 *  recruits only after ethics AND regulatory approval, with
 *  conflicts of interest declared and funding on record.
 *  Participants exist in the research record under a code only —
 *  research data is separated from care data — and enrollment
 *  requires confirmed eligibility and documented informed consent
 *  before any visit or sample. Withdrawal is immediate: no new
 *  research activity afterwards. Serious adverse events must be
 *  reported to the ethics committee; an unreported SAE is a breach
 *  surfaced on the record. */
@RestController @RequestMapping("/api/clinical-trials")
public class ClinicalTrialsController {
  private final TrialProtocolRepository trials; private final TrialParticipantRepository participants;
  private final TrialVisitRepository visits; private final TrialAdverseEventRepository events; private final AuditService audit;
  public ClinicalTrialsController(TrialProtocolRepository trials, TrialParticipantRepository participants,
                                  TrialVisitRepository visits, TrialAdverseEventRepository events, AuditService audit) {
    this.trials=trials; this.participants=participants; this.visits=visits; this.events=events; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private TrialProtocol trial(UUID id) {
    return trials.findById(id).orElseThrow(() -> BizException.notFound("trial_not_found","التجربة غير موجودة"));
  }
  private TrialParticipant participant(UUID id) {
    return participants.findById(id).orElseThrow(() -> BizException.notFound("participant_not_found","المشارك غير موجود"));
  }

  public record TrialRequest(String title, String phase, String sponsor, String protocolVersion, String principalInvestigator,
                             String ethicsApprovalRef, Boolean ethicsApproved, String regulatoryApprovalRef, Boolean regulatoryApproved,
                             Boolean coiDeclared, String fundingRef) {}
  @PostMapping("/")
  Map<String,Object> create(@RequestBody TrialRequest r) {
    var p = principal();
    var t = trials.save(new TrialProtocol(UUID.fromString(p.facilityId()), p.governorate(), r.title(), r.phase(), r.sponsor(), r.protocolVersion(),
      r.principalInvestigator(), r.ethicsApprovalRef(), Boolean.TRUE.equals(r.ethicsApproved()), r.regulatoryApprovalRef(),
      Boolean.TRUE.equals(r.regulatoryApproved()), Boolean.TRUE.equals(r.coiDeclared()), r.fundingRef()));
    audit.record("trial.registered","trial_protocol",t.getId().toString(),"{}");
    return Map.of("ok",true,"trial",t);
  }
  @PostMapping("/{id}/start-recruitment")
  Map<String,Object> recruit(@PathVariable UUID id) {
    principal(); var t = trial(id);
    if (!TrialRules.recruitmentPermitted(t.isEthicsApproved(), t.isRegulatoryApproved(), t.isCoiDeclared()))
      throw BizException.conflict("recruitment_blocked","التوظيف يتطلب موافقة الأخلاقيات والموافقة التنظيمية وإعلان تضارب المصالح");
    t.setStatus("recruiting"); trials.save(t);
    audit.record("trial.recruitment.started","trial_protocol",id.toString(),"{}");
    return Map.of("ok",true,"trial",t);
  }
  public record EnrollRequest(String participantCode, Boolean eligibilityConfirmed) {}
  @PostMapping("/{id}/participants")
  Map<String,Object> enroll(@PathVariable UUID id, @RequestBody EnrollRequest r) {
    var p = principal(); var t = trial(id);
    if (!"recruiting".equals(t.getStatus()))
      throw BizException.conflict("not_recruiting","التجربة ليست في حالة توظيف");
    var pt = participants.save(new TrialParticipant(t.getFacilityId(), p.governorate(), id, r.participantCode(), Boolean.TRUE.equals(r.eligibilityConfirmed())));
    audit.record("trial.participant.enrolled","trial_participant",pt.getId().toString(),"{}");
    return Map.of("ok",true,"participant",pt,"data_separation","research record holds the participant code only — no care-record identifiers");
  }
  @PostMapping("/participants/{pid}/consent")
  Map<String,Object> consent(@PathVariable UUID pid) {
    principal(); var pt = participant(pid);
    if (!pt.isEligibilityConfirmed())
      throw BizException.conflict("eligibility_first","الموافقة تُوثق بعد تأكيد الأهلية");
    pt.consent(); participants.save(pt);
    audit.record("trial.consent.documented","trial_participant",pid.toString(),"{}");
    return Map.of("ok",true,"participant",pt);
  }
  public record WithdrawRequest(String reason) {}
  @PostMapping("/participants/{pid}/withdraw")
  Map<String,Object> withdraw(@PathVariable UUID pid, @RequestBody WithdrawRequest r) {
    principal(); var pt = participant(pid);
    pt.withdraw(r.reason()); participants.save(pt);
    audit.record("trial.participant.withdrew","trial_participant",pid.toString(),"{}");
    return Map.of("ok",true,"participant",pt,"effect","withdrawal is immediate — no new research activity");
  }
  public record VisitRequest(String visitLabel, OffsetDateTime scheduledAt, String samplesRef) {}
  @PostMapping("/participants/{pid}/visits")
  Map<String,Object> visit(@PathVariable UUID pid, @RequestBody VisitRequest r) {
    var p = principal(); var pt = participant(pid);
    if (!TrialRules.researchActivityPermitted(pt.getConsentStatus()))
      throw BizException.conflict("no_active_consent","لا نشاط بحثي دون موافقة نشطة — الانسحاب فوري الأثر");
    var v = visits.save(new TrialVisit(UUID.fromString(p.facilityId()), p.governorate(), pid, r.visitLabel(), r.scheduledAt(), r.samplesRef()));
    audit.record("trial.visit.completed","trial_visit",v.getId().toString(),"{}");
    return Map.of("ok",true,"visit",v);
  }
  public record AeRequest(String description, String severity, Boolean reportedToEthics, String actionTaken) {}
  @PostMapping("/participants/{pid}/adverse-events")
  Map<String,Object> ae(@PathVariable UUID pid, @RequestBody AeRequest r) {
    var p = principal(); participant(pid);
    var e = events.save(new TrialAdverseEvent(UUID.fromString(p.facilityId()), p.governorate(), pid, r.description(), r.severity(),
      Boolean.TRUE.equals(r.reportedToEthics()), r.actionTaken()));
    audit.record("trial.adverse_event.recorded","trial_adverse_event",e.getId().toString(),"{}");
    return Map.of("ok",true,"adverse_event",e,"sae_ethics_breach",TrialRules.saeBreach(r.severity(), Boolean.TRUE.equals(r.reportedToEthics())));
  }
}
