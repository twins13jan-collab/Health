package eg.gov.health.platform.donationcampaigns;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Blood donation campaigns (module 155). Campaigns, sites and
 *  capacity are managed; donors are registered with eligibility
 *  and donation history checked on the day; communication and
 *  reminders are tracked; results and referrals are CONFIDENTIAL
 *  to the donor; and collected units are allocated against
 *  demand, never beyond what was collected. */
@RestController @RequestMapping("/api/donation-campaigns")
public class DonationCampaignsController {
  private final DonationCampaignRepository campaigns; private final DonorRegistrationRepository donors;
  private final DonationResultRepository results; private final UnitAllocationRepository allocations; private final AuditService audit;
  public DonationCampaignsController(DonationCampaignRepository campaigns, DonorRegistrationRepository donors,
                                     DonationResultRepository results, UnitAllocationRepository allocations, AuditService audit) {
    this.campaigns=campaigns; this.donors=donors; this.results=results; this.allocations=allocations; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private DonationCampaign campaign(UUID id) {
    return campaigns.findById(id).orElseThrow(() -> BizException.notFound("campaign_not_found","الحملة غير موجودة"));
  }

  public record CampaignRequest(String name, String site, Integer capacity, LocalDate campaignDate) {}
  @PostMapping("/campaigns")
  Map<String,Object> create(@RequestBody CampaignRequest r) {
    var p = principal();
    var c = campaigns.save(new DonationCampaign(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.site(), r.capacity(), r.campaignDate()));
    return Map.of("ok",true,"campaign",c);
  }
  public record DonorRequest(String donorRef, LocalDate lastDonationDate, Boolean ageOk, Boolean weightOk, Boolean hemoglobinOk, Boolean healthOk) {}
  @PostMapping("/campaigns/{id}/donors")
  Map<String,Object> registerDonor(@PathVariable UUID id, @RequestBody DonorRequest r) {
    var p = principal(); var c = campaign(id);
    boolean intervalOk = DonationRules.intervalRespected(r.lastDonationDate(), c.getCampaignDate());
    boolean eligible = DonationRules.donorEligible(r.ageOk(), r.weightOk(), r.hemoglobinOk(), r.healthOk(), intervalOk);
    String reason = eligible ? null : (!intervalOk ? "donation interval under 90 days" : "eligibility check failed on the day");
    var d = donors.save(new DonorRegistration(UUID.fromString(p.facilityId()), p.governorate(), id, r.donorRef(), r.lastDonationDate(),
      r.ageOk(), r.weightOk(), r.hemoglobinOk(), r.healthOk(), eligible, reason));
    audit.record("donation.donor.registered","donor_registration",d.getId().toString(),"{}");
    return Map.of("ok",true,"registration",d,"eligible",eligible,"deferred_reason",reason == null ? "" : reason);
  }
  public record CollectRequest(Integer units) {}
  @PostMapping("/campaigns/{id}/collect")
  Map<String,Object> collect(@PathVariable UUID id, @RequestBody CollectRequest r) {
    principal(); var c = campaign(id);
    c.addUnits(r.units() == null ? 0 : r.units()); campaigns.save(c);
    return Map.of("ok",true,"campaign",c,"units_collected",c.getUnitsCollected());
  }
  public record ResultRequest(String testResult, Boolean donorNotifiedConfidentially, Boolean referralCreated) {}
  @PostMapping("/donors/{regId}/result")
  Map<String,Object> result(@PathVariable UUID regId, @RequestBody ResultRequest r) {
    var p = principal();
    var d = donors.findById(regId).orElseThrow(() -> BizException.notFound("donor_not_found","المتبرع غير موجود"));
    if (!d.isEligible()) throw BizException.conflict("not_donated","نتيجة على متبرع تبرع فعليًا");
    if ("reactive".equals(r.testResult()) && !Boolean.TRUE.equals(r.donorNotifiedConfidentially()))
      throw BizException.conflict("confidential_notification","النتيجة التفاعلية تُبلَّغ للمتبرع بسرية أولًا مع الإحالة — لا تظهر في تقارير الحملة أبدًا");
    var res = results.save(new DonationResult(UUID.fromString(p.facilityId()), p.governorate(), regId, r.testResult(),
      Boolean.TRUE.equals(r.donorNotifiedConfidentially()), Boolean.TRUE.equals(r.referralCreated())));
    audit.record("donation.result.recorded","donation_result",res.getId().toString(),"{}");
    return Map.of("ok",true,"result_record",res,"confidentiality","donor results are confidential to the donor — campaign reports carry counts only");
  }
  public record AllocRequest(String allocatedTo, Integer unitsAllocated) {}
  @PostMapping("/campaigns/{id}/allocations")
  Map<String,Object> allocate(@PathVariable UUID id, @RequestBody AllocRequest r) {
    var p = principal(); var c = campaign(id);
    int sum = allocations.findByCampaignId(id).stream().mapToInt(a -> a.getUnitsAllocated() == null ? 0 : a.getUnitsAllocated()).sum();
    if (!DonationRules.allocationPermitted(sum, r.unitsAllocated() == null ? 0 : r.unitsAllocated(), c.getUnitsCollected()))
      throw BizException.conflict("over_allocation","التوزيع ضد الطلب وفي حدود المجمع فعليًا — لا وحدات من الهواء");
    var a = allocations.save(new UnitAllocation(UUID.fromString(p.facilityId()), p.governorate(), id, r.allocatedTo(), r.unitsAllocated()));
    audit.record("donation.units.allocated","unit_allocation",a.getId().toString(),"{}");
    return Map.of("ok",true,"allocation",a,"total_allocated",sum + (r.unitsAllocated() == null ? 0 : r.unitsAllocated()),"collected",c.getUnitsCollected());
  }
}
