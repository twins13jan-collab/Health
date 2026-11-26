package eg.gov.health.platform.medicaltourism;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Medical tourism & international patients (module 75).
 *  International patient requests, medical files, translation
 *  and consents are managed; appointments, financial estimates,
 *  accommodation, transport and logistics are coordinated;
 *  international referrals, medical reports and communication
 *  with referring bodies are handled; data is protected and
 *  transferred across borders only on a lawful basis; and
 *  post-return care is followed up with the home provider as
 *  the patient permits. */
@RestController @RequestMapping("/api/medical-tourism")
public class MedicalTourismController {
  private final IntlPatientRequestRepository requests; private final CrossBorderTransferRepository transfers;
  private final IntlFollowupContactRepository followups; private final AuditService audit;
  public MedicalTourismController(IntlPatientRequestRepository requests, CrossBorderTransferRepository transfers,
                                  IntlFollowupContactRepository followups, AuditService audit) {
    this.requests=requests; this.transfers=transfers; this.followups=followups; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private IntlPatientRequest request(UUID id) {
    return requests.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
  }

  public record IntlRequestBody(String patientRef, String country, Boolean fileTranslated, Boolean consentObtained, Boolean financialEstimateApproved) {}
  @PostMapping("/requests")
  Map<String,Object> create(@RequestBody IntlRequestBody r) {
    var p = principal();
    var req = requests.save(new IntlPatientRequest(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.country(),
      Boolean.TRUE.equals(r.fileTranslated()), Boolean.TRUE.equals(r.consentObtained()), Boolean.TRUE.equals(r.financialEstimateApproved())));
    return Map.of("ok",true,"request",req);
  }
  @PostMapping("/requests/{id}/schedule")
  Map<String,Object> schedule(@PathVariable UUID id) {
    principal(); var req = request(id);
    if (!TourismRules.schedulable(req.isFileTranslated(), req.isConsentObtained(), req.isFinancialEstimateApproved()))
      throw BizException.conflict("not_schedulable","مريض دولي لا يُجدول قبل ملف مترجم وموافقة وتقدير مالي معتمد — لا مفاجآت بلغة أو حقوق أو مال");
    req.schedule(); requests.save(req);
    return Map.of("ok",true,"request",req);
  }
  public record TransferBody(String dataDescription, String legalBasis, Boolean patientConsent) {}
  @PostMapping("/requests/{id}/transfers")
  Map<String,Object> transfer(@PathVariable UUID id, @RequestBody TransferBody r) {
    var p = principal(); request(id);
    if (!TourismRules.transferLawful(r.legalBasis(), Boolean.TRUE.equals(r.patientConsent())))
      throw BizException.conflict("unlawful_transfer","بيانات صحية لا تعبر الحدود إلا بأساس قانوني وموافقة المريض معًا — أحدهما وحده لا يكفي");
    var t = transfers.save(new CrossBorderTransfer(UUID.fromString(p.facilityId()), p.governorate(), id, r.dataDescription(), r.legalBasis(), true));
    audit.record("tourism.data.transferred","cross_border_transfer",t.getId().toString(),"{}");
    return Map.of("ok",true,"transfer",t);
  }
  public record FollowupBody(String homeProvider, Boolean patientPermission) {}
  @PostMapping("/requests/{id}/followups")
  Map<String,Object> followup(@PathVariable UUID id, @RequestBody FollowupBody r) {
    var p = principal(); request(id);
    if (!TourismRules.followupPermitted(Boolean.TRUE.equals(r.patientPermission())))
      throw BizException.conflict("no_permission","تواصل مع مقدم الرعاية ببلد المريض بإذنه فقط");
    var f = followups.save(new IntlFollowupContact(UUID.fromString(p.facilityId()), p.governorate(), id, r.homeProvider(), true, true));
    audit.record("tourism.followup.contacted","intl_followup_contact",f.getId().toString(),"{}");
    return Map.of("ok",true,"followup",f);
  }
}
