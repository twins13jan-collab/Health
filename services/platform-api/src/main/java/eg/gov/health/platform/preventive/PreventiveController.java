package eg.gov.health.platform.preventive;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Preventive medicine & mass screening (module 151). Screening
 *  programmes and eligibility are managed; invitations, consents
 *  and appointments are tracked; positive results enter the
 *  confirmatory-and-referral pathway and close only through it;
 *  non-attendance is followed up; coverage and effectiveness are
 *  evaluated from real attendance. */
@RestController @RequestMapping("/api/preventive")
public class PreventiveController {
  private final ScreeningProgramRepository programs; private final ScreeningInvitationRepository invitations;
  private final ScreeningResultRepository results; private final AuditService audit;
  public PreventiveController(ScreeningProgramRepository programs, ScreeningInvitationRepository invitations,
                              ScreeningResultRepository results, AuditService audit) {
    this.programs=programs; this.invitations=invitations; this.results=results; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record ProgramRequest(String name, String targetCondition, String eligibilityCriteria) {}
  @PostMapping("/programs")
  Map<String,Object> program(@RequestBody ProgramRequest r) {
    var p = principal();
    var pr = programs.save(new ScreeningProgram(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.targetCondition(), r.eligibilityCriteria()));
    return Map.of("ok",true,"program",pr);
  }
  public record InviteRequest(String personRef, Boolean eligible, Boolean consentGiven, OffsetDateTime appointmentAt) {}
  @PostMapping("/programs/{id}/invitations")
  Map<String,Object> invite(@PathVariable UUID id, @RequestBody InviteRequest r) {
    var p = principal();
    programs.findById(id).orElseThrow(() -> BizException.notFound("program_not_found","البرنامج غير موجود"));
    var inv = invitations.save(new ScreeningInvitation(UUID.fromString(p.facilityId()), p.governorate(), id, r.personRef(),
      Boolean.TRUE.equals(r.eligible()), Boolean.TRUE.equals(r.consentGiven()), r.appointmentAt()));
    audit.record("preventive.invited","screening_invitation",inv.getId().toString(),"{}");
    return Map.of("ok",true,"invitation",inv);
  }
  public record AttendRequest(String attendance) {}
  @PostMapping("/invitations/{iid}/attendance")
  Map<String,Object> attend(@PathVariable UUID iid, @RequestBody AttendRequest r) {
    principal();
    var inv = invitations.findById(iid).orElseThrow(() -> BizException.notFound("invitation_not_found","الدعوة غير موجودة"));
    inv.setAttendance(r.attendance()); invitations.save(inv);
    return Map.of("ok",true,"invitation",inv,"follow_up","no_show".equals(r.attendance()) ? "no-show is followed up and re-invited — silence is not a screening outcome" : "recorded");
  }
  public record ResultRequest(String result) {}
  @PostMapping("/invitations/{iid}/screen")
  Map<String,Object> screen(@PathVariable UUID iid, @RequestBody ResultRequest r) {
    var p = principal();
    var inv = invitations.findById(iid).orElseThrow(() -> BizException.notFound("invitation_not_found","الدعوة غير موجودة"));
    if (!PreventiveRules.screeningPermitted(inv.isEligible(), inv.isConsentGiven()))
      throw BizException.conflict("screening_blocked","الفحص الجماعي للمؤهلين وبموافقة — لا فحص لغير مؤهل أو بلا موافقة");
    if (!"attended".equals(inv.getAttendance())) throw BizException.conflict("not_attended","النتيجة على حاضر فعليًا");
    var res = results.save(new ScreeningResult(UUID.fromString(p.facilityId()), p.governorate(), iid, r.result()));
    audit.record("preventive.screened","screening_result",res.getId().toString(),"{}");
    return Map.of("ok",true,"result",res,"meaning","positive".equals(r.result())
      ? "a positive SCREEN is not a diagnosis — it opens the confirmatory pathway" : "negative screen recorded");
  }
  @PostMapping("/results/{rid}/confirm")
  Map<String,Object> confirm(@PathVariable UUID rid) {
    principal();
    var res = results.findById(rid).orElseThrow(() -> BizException.notFound("result_not_found","النتيجة غير موجودة"));
    res.confirm(); results.save(res);
    return Map.of("ok",true,"result",res);
  }
  @PostMapping("/results/{rid}/refer")
  Map<String,Object> refer(@PathVariable UUID rid) {
    principal();
    var res = results.findById(rid).orElseThrow(() -> BizException.notFound("result_not_found","النتيجة غير موجودة"));
    res.refer(); results.save(res);
    return Map.of("ok",true,"result",res);
  }
  @PostMapping("/results/{rid}/close-pathway")
  Map<String,Object> closePathway(@PathVariable UUID rid) {
    principal();
    var res = results.findById(rid).orElseThrow(() -> BizException.notFound("result_not_found","النتيجة غير موجودة"));
    if (!PreventiveRules.pathwayClosable(res.getResult(), res.isConfirmatoryDone(), res.isReferralCreated()))
      throw BizException.conflict("pathway_open","مسار النتيجة الإيجابية لا يُغلق قبل الفحص التأكيدي والإحالة");
    res.closePathway(); results.save(res);
    return Map.of("ok",true,"result",res,"pathway_closed",true);
  }
  @GetMapping("/programs/{id}/coverage")
  Map<String,Object> coverage(@PathVariable UUID id) {
    principal();
    long invited = invitations.countByProgramId(id);
    long attended = invitations.countByProgramIdAndAttendance(id, "attended");
    long noShow = invitations.countByProgramIdAndAttendance(id, "no_show");
    return Map.of("ok",true,"invited",invited,"attended",attended,"no_show",noShow,
      "coverage_pct",PreventiveRules.coverage(attended, invited));
  }
}
