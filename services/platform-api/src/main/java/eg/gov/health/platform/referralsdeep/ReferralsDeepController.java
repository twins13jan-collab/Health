package eg.gov.health.platform.referralsdeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Unified appointments, referrals & waiting lists
 *  (module 110). A unified appointment directory by service
 *  and real capacity; referrals carry the clinical reason,
 *  attachments and priority; acceptance or rejection comes
 *  with a reason and an alternative; waiting lists run on
 *  declared fair rules; booking, cancellation, confirmation
 *  and reminders are managed; reverse referrals complete the
 *  care loop; and referral time and pathway completion are
 *  measured. */
@RestController @RequestMapping("/api/referrals-deep")
public class ReferralsDeepController {
  private final ReferralRequestRepository referrals; private final WaitlistEntryRepository waitlist; private final AuditService audit;
  public ReferralsDeepController(ReferralRequestRepository referrals, WaitlistEntryRepository waitlist, AuditService audit) {
    this.referrals=referrals; this.waitlist=waitlist; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ReferralRequest referral(UUID id) {
    return referrals.findById(id).orElseThrow(() -> BizException.notFound("referral_not_found","الإحالة غير موجودة"));
  }

  public record ReferralBody(String patientRef, String clinicalReason, Boolean attachmentsComplete, String priority) {}
  @PostMapping("/referrals")
  Map<String,Object> create(@RequestBody ReferralBody r) {
    var p = principal();
    if (!ReferralRules.submittable(r.clinicalReason(), Boolean.TRUE.equals(r.attachmentsComplete())))
      throw BizException.conflict("referral_incomplete","إحالة تسافر بسببها السريري ومرفقاتها — اسم مجرد ليس إحالة");
    var ref = referrals.save(new ReferralRequest(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.clinicalReason(), true, r.priority()));
    return Map.of("ok",true,"referral",ref);
  }
  @PostMapping("/referrals/{id}/accept")
  Map<String,Object> accept(@PathVariable UUID id) {
    principal(); var ref = referral(id);
    ref.accept(); referrals.save(ref);
    return Map.of("ok",true,"referral",ref);
  }
  public record RejectBody(String reason, String alternativeOffered) {}
  @PostMapping("/referrals/{id}/reject")
  Map<String,Object> reject(@PathVariable UUID id, @RequestBody RejectBody r) {
    principal(); var ref = referral(id);
    if (!ReferralRules.rejectionComplete(r.reason(), r.alternativeOffered()))
      throw BizException.conflict("rejection_incomplete","رفض إحالة قرار فقط بسبب وبديل معًا — رفض بلا بديل طريق مسدود يرتدي ثوب إجابة");
    ref.reject(r.reason(), r.alternativeOffered()); referrals.save(ref);
    audit.record("referral.rejected","referral_request",id.toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  public record WaitBody(String serviceRef, String patientRef, Integer priorityScore, LocalDate waitingSince, String fairnessRuleRef) {}
  @PostMapping("/waitlist")
  Map<String,Object> wait(@RequestBody WaitBody r) {
    var p = principal();
    if (r.fairnessRuleRef()==null || r.fairnessRuleRef().isBlank())
      throw BizException.badRequest("fairness_rule_required","قائمة انتظار تعمل بقاعدة عدالة معلنة — الترتيب لا يكون بمن اتصل بمن");
    var w = waitlist.save(new WaitlistEntry(UUID.fromString(p.facilityId()), p.governorate(), r.serviceRef(), r.patientRef(),
      r.priorityScore(), r.waitingSince(), r.fairnessRuleRef()));
    return Map.of("ok",true,"entry",w);
  }
  @GetMapping("/waitlist/order")
  Map<String,Object> order(@RequestParam String serviceRef) {
    principal();
    var entries = new ArrayList<>(waitlist.findByServiceRef(serviceRef));
    entries.sort((a, b) -> ReferralRules.compareWaitlist(
      a.getPriorityScore() == null ? 0 : a.getPriorityScore(), a.getWaitingSince(),
      b.getPriorityScore() == null ? 0 : b.getPriorityScore(), b.getWaitingSince()));
    return Map.of("ok",true,"service_ref",serviceRef,
      "ordered_patients",entries.stream().map(WaitlistEntry::getPatientRef).toList(),
      "rule","clinical priority first, then longest waiting — computed in the open");
  }
}
