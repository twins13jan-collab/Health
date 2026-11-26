package eg.gov.health.platform.organbank;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.time.*; import java.util.*;
/** Tissues, organs, cells & biological products (module 129).
 *  Donation, evaluation, consent and allocation follow the law:
 *  allocation only with documented consent, completed medical
 *  evaluation, a clean coercion screen and committee approval. Any
 *  unethical-incentive flag blocks the case outright — trafficking
 *  prevention is a hard stop. The chain of custody times the cold
 *  ischemia clock per graft type and flags temperature excursions;
 *  every graft is traceable from source to recipient, discards are
 *  investigated, and post-transplant outcomes are followed. */
@RestController @RequestMapping("/api/organ-bank")
public class OrganBankController {
  private final DonationCaseRepository cases; private final AllocationRecordRepository allocs;
  private final CustodyChainEventRepository chain; private final GraftOutcomeRepository outcomes;
  private final OrganDiscardRecordRepository discards; private final AuditService audit;
  public OrganBankController(DonationCaseRepository cases, AllocationRecordRepository allocs, CustodyChainEventRepository chain,
                             GraftOutcomeRepository outcomes, OrganDiscardRecordRepository discards, AuditService audit) {
    this.cases=cases; this.allocs=allocs; this.chain=chain; this.outcomes=outcomes; this.discards=discards; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private DonationCase kase(UUID id) {
    return cases.findById(id).orElseThrow(() -> BizException.notFound("donation_not_found","حالة التبرع غير موجودة"));
  }

  public record DonationRequest(String donorCode, String donationType, String organOrTissue, String consentBasis,
                                Boolean consentDocumented, Boolean coercionScreened, Boolean incentiveFlag,
                                String medicalEvaluation, Boolean evaluationComplete) {}
  @PostMapping("/donations")
  Map<String,Object> donate(@RequestBody DonationRequest r) {
    var p = principal();
    var d = cases.save(new DonationCase(UUID.fromString(p.facilityId()), p.governorate(), r.donorCode(), r.donationType(), r.organOrTissue(),
      r.consentBasis(), Boolean.TRUE.equals(r.consentDocumented()), Boolean.TRUE.equals(r.coercionScreened()),
      Boolean.TRUE.equals(r.incentiveFlag()), r.medicalEvaluation(), Boolean.TRUE.equals(r.evaluationComplete())));
    audit.record("organ.donation.opened","donation_case",d.getId().toString(),"{}");
    return Map.of("ok",true,"donation",d,"case_blocked",OrganRules.caseBlocked(d.isIncentiveFlag(), d.isCoercionScreened()),
      "block_reason",d.isIncentiveFlag() ? "unethical incentive flag — case blocked, anti-trafficking"
        : (!d.isCoercionScreened() ? "coercion screen pending — allocation barred until screened clean" : "none"));
  }
  public record CommitteeRequest(String decision) {}
  @PostMapping("/donations/{id}/committee")
  Map<String,Object> committee(@PathVariable UUID id, @RequestBody CommitteeRequest r) {
    principal(); var d = kase(id);
    d.committee(r.decision()); cases.save(d);
    audit.record("organ.committee.decided","donation_case",id.toString(),"{}");
    return Map.of("ok",true,"donation",d);
  }
  public record AllocationRequest(String recipientRef, String matchBasis) {}
  @PostMapping("/donations/{id}/allocate")
  Map<String,Object> allocate(@PathVariable UUID id, @RequestBody AllocationRequest r) {
    var p = principal(); var d = kase(id);
    boolean committeeApproved = "approved".equals(d.getCommitteeDecision());
    if (!OrganRules.allocationPermitted(d.isConsentDocumented(), d.isEvaluationComplete(), d.isCoercionScreened(), d.isIncentiveFlag(), committeeApproved))
      throw BizException.conflict("allocation_blocked","التخصيص يتطلب موافقة موثقة وتقييمًا طبيًا مكتملًا وفحص إكراه نظيفًا واعتماد اللجنة");
    var a = allocs.save(new AllocationRecord(d.getFacilityId(), p.governorate(), id, r.recipientRef(), r.matchBasis(), p.email(), true));
    d.setStatus("allocated"); cases.save(d);
    audit.record("organ.allocated","allocation_record",a.getId().toString(),"{}");
    return Map.of("ok",true,"allocation",a,"traceability","source donor " + "-> recipient " + r.recipientRef());
  }
  public record CustodyRequest(String stage, String custodian, BigDecimal temperatureC, OffsetDateTime eventAt, Boolean ischemiaStart) {}
  @PostMapping("/donations/{id}/custody")
  Map<String,Object> custody(@PathVariable UUID id, @RequestBody CustodyRequest r) {
    var p = principal(); var d = kase(id);
    var e = chain.save(new CustodyChainEvent(d.getFacilityId(), p.governorate(), id, r.stage(), r.custodian(), r.temperatureC(), r.eventAt(), Boolean.TRUE.equals(r.ischemiaStart())));
    var events = chain.findByDonationIdOrderByEventAtAsc(id);
    OffsetDateTime start = events.stream().filter(CustodyChainEvent::isIschemiaStart).map(CustodyChainEvent::getEventAt).findFirst().orElse(null);
    boolean breached = start != null && OrganRules.ischemiaBreached(d.getOrganOrTissue(), start, e.getEventAt());
    audit.record("organ.custody."+r.stage(),"custody_chain_event",e.getId().toString(),"{}");
    return Map.of("ok",true,"event",e,
      "temperature_excursion",OrganRules.temperatureExcursion(d.getOrganOrTissue(), r.temperatureC()),
      "ischemia_minutes",start==null ? -1 : OrganRules.ischemiaMinutes(start, e.getEventAt()),
      "ischemia_breached",breached);
  }
  public record OutcomeRequest(String recipientRef, String outcome, String followUpNote) {}
  @PostMapping("/donations/{id}/outcome")
  Map<String,Object> outcome(@PathVariable UUID id, @RequestBody OutcomeRequest r) {
    var p = principal(); var d = kase(id);
    var o = outcomes.save(new GraftOutcome(d.getFacilityId(), p.governorate(), id, r.recipientRef(), r.outcome(), r.followUpNote(), p.email()));
    if ("allocated".equals(d.getStatus())) { d.setStatus("transplanted"); cases.save(d); }
    audit.record("organ.outcome.recorded","graft_outcome",o.getId().toString(),"{}");
    return Map.of("ok",true,"outcome_record",o);
  }
  public record DiscardRequest(String reason, String investigatedBy, String investigation) {}
  @PostMapping("/donations/{id}/discard")
  Map<String,Object> discard(@PathVariable UUID id, @RequestBody DiscardRequest r) {
    var p = principal(); var d = kase(id);
    if (r.investigatedBy()==null || r.investigation()==null)
      throw BizException.badRequest("discard_investigation","تلف أو رفض العضو يستلزم تحقيقًا موثقًا");
    var x = discards.save(new OrganDiscardRecord(d.getFacilityId(), p.governorate(), id, r.reason(), r.investigatedBy(), r.investigation()));
    d.setStatus("discarded"); cases.save(d);
    audit.record("organ.discarded","organ_discard_record",x.getId().toString(),"{}");
    return Map.of("ok",true,"discard",x);
  }
}
