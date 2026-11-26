package eg.gov.health.platform.citizenportal;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Citizen portal & multi-channel communication (module 111).
 *  Accounts are identity-verified before anything is disclosed.
 *  Representatives act only inside an approved, unexpired scope.
 *  Results, prescriptions and summaries are released per permission
 *  and every disclosure is logged with its channel. Requests,
 *  inquiries and complaints are tracked to an answer. Communication
 *  preferences and consents govern every outbound message — a
 *  clinical alert never travels on a channel the citizen did not
 *  enable for clinical alerts — and accessible formats are part of
 *  the account. */
@RestController @RequestMapping("/api/citizen-portal")
public class CitizenPortalController {
  private final CitizenAccountRepository accounts; private final CitizenRepresentativeRepository representatives;
  private final PortalDisclosureRepository disclosures; private final CitizenRequestRepository requests;
  private final CommunicationPreferenceRepository preferences; private final OutboundMessageRepository messages;
  private final AuditService audit;
  public CitizenPortalController(CitizenAccountRepository accounts, CitizenRepresentativeRepository representatives,
                                 PortalDisclosureRepository disclosures, CitizenRequestRepository requests,
                                 CommunicationPreferenceRepository preferences, OutboundMessageRepository messages, AuditService audit) {
    this.accounts=accounts; this.representatives=representatives; this.disclosures=disclosures;
    this.requests=requests; this.preferences=preferences; this.messages=messages; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private CitizenAccount account(UUID id) {
    return accounts.findById(id).orElseThrow(() -> BizException.notFound("account_not_found","حساب المواطن غير موجود"));
  }

  public record AccountRequest(String citizenRef, String fullName) {}
  @PostMapping("/accounts")
  Map<String,Object> open(@RequestBody AccountRequest r) {
    var p = principal();
    var a = accounts.save(new CitizenAccount(UUID.fromString(p.facilityId()), p.governorate(), r.citizenRef(), r.fullName()));
    audit.record("portal.account.opened","citizen_account",a.getId().toString(),"{}");
    return Map.of("ok",true,"account",a,"note","no disclosure before identity verification");
  }
  @PostMapping("/accounts/{id}/verify-identity")
  Map<String,Object> verify(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var a = account(id); a.verify(body.getOrDefault("method","national_id_otp")); accounts.save(a);
    audit.record("portal.identity.verified","citizen_account",id.toString(),"{}");
    return Map.of("ok",true,"account",a);
  }
  public record RepRequest(String representativeName, String relationship, String scope, LocalDate validUntil, Boolean approved) {}
  @PostMapping("/accounts/{id}/representatives")
  Map<String,Object> representative(@PathVariable UUID id, @RequestBody RepRequest r) {
    var p = principal(); var a = account(id);
    var rep = representatives.save(new CitizenRepresentative(a.getFacilityId(), p.governorate(), id, r.representativeName(), r.relationship(), r.scope(), r.validUntil(), Boolean.TRUE.equals(r.approved())));
    audit.record("portal.representative.added","citizen_representative",rep.getId().toString(),"{}");
    return Map.of("ok",true,"representative",rep);
  }
  public record AccessRequest(String resourceType, String resourceRef, String accessedBy, UUID representativeId, String channel) {}
  @PostMapping("/accounts/{id}/access")
  Map<String,Object> access(@PathVariable UUID id, @RequestBody AccessRequest r) {
    var p = principal(); var a = account(id);
    boolean allowed;
    if (r.representativeId()!=null) {
      var rep = representatives.findById(r.representativeId())
        .orElseThrow(() -> BizException.notFound("representative_not_found","الممثل غير موجود"));
      allowed = PortalRules.representativeAccess(rep.isApproved(), rep.getValidUntil(), rep.getScope(), r.resourceType(), LocalDate.now());
    } else {
      allowed = PortalRules.selfAccess(a.isIdentityVerified(), a.getStatus());
    }
    if (!allowed)
      throw BizException.forbidden("الإتاحة تتطلب هوية موثقة أو تمثيلًا معتمدًا ساريًا ضمن نطاقه");
    var d = disclosures.save(new PortalDisclosure(a.getFacilityId(), p.governorate(), id, r.resourceType(), r.resourceRef(), r.accessedBy(), r.channel()));
    audit.record("portal.resource.disclosed","portal_disclosure",d.getId().toString(),"{}");
    return Map.of("ok",true,"disclosure",d);
  }
  public record PrefRequest(String channel, String language, String accessibleFormat, Boolean clinicalAlerts, Boolean enabled) {}
  @PostMapping("/accounts/{id}/preferences")
  Map<String,Object> preference(@PathVariable UUID id, @RequestBody PrefRequest r) {
    var p = principal(); var a = account(id);
    var pref = preferences.save(new CommunicationPreference(a.getFacilityId(), p.governorate(), id, r.channel(), r.language(),
      r.accessibleFormat(), Boolean.TRUE.equals(r.clinicalAlerts()), !Boolean.FALSE.equals(r.enabled())));
    audit.record("portal.preference.set","communication_preference",pref.getId().toString(),"{}");
    return Map.of("ok",true,"preference",pref);
  }
  public record MessageRequest(String channel, String template, Boolean clinical) {}
  @PostMapping("/accounts/{id}/messages")
  Map<String,Object> message(@PathVariable UUID id, @RequestBody MessageRequest r) {
    var p = principal(); var a = account(id);
    var pref = preferences.findByAccountIdAndChannel(id, r.channel());
    boolean consented = pref.isPresent() && PortalRules.channelConsented(true, pref.get().isEnabled(), Boolean.TRUE.equals(r.clinical()), pref.get().isClinicalAlerts());
    // A message on an unconsented channel is blocked and the block itself is the record.
    var m = messages.save(new OutboundMessage(a.getFacilityId(), p.governorate(), id, r.channel(), r.template(), Boolean.TRUE.equals(r.clinical()), consented ? "sent" : "blocked"));
    audit.record("portal.message." + m.getStatus(),"outbound_message",m.getId().toString(),"{}");
    return Map.of("ok",true,"message",m,"delivered",consented,
      "note", consented ? "sent on a citizen-enabled channel" : "blocked: channel not enabled by the citizen for this message type");
  }
  public record RequestRequest(String kind, String subject, String body) {}
  @PostMapping("/accounts/{id}/requests")
  Map<String,Object> request(@PathVariable UUID id, @RequestBody RequestRequest r) {
    var p = principal(); var a = account(id);
    var rq = requests.save(new CitizenRequest(a.getFacilityId(), p.governorate(), id, r.kind(), r.subject(), r.body()));
    audit.record("portal.request.opened","citizen_request",rq.getId().toString(),"{}");
    return Map.of("ok",true,"request",rq);
  }
  @PostMapping("/requests/{id}/respond")
  Map<String,Object> respond(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal();
    var rq = requests.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    rq.respond(p.email(), body.get("response")); requests.save(rq);
    audit.record("portal.request.answered","citizen_request",id.toString(),"{}");
    return Map.of("ok",true,"request",rq);
  }
}
