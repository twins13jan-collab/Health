package eg.gov.health.platform.deathdata;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Death data & its review (module 159). Death registration
 *  with its time, place and source; causes of death under the
 *  approved coding; review of deaths and avoidable-mortality
 *  indicators; official statistical linkage through approved
 *  channels; conflict correction with a historical record; and
 *  protection of the deceased's and the family's data. */
@RestController @RequestMapping("/api/death-data")
public class DeathDataController {
  private final DeathRegistrationRepository registrations; private final DeathCorrectionRepository corrections; private final AuditService audit;
  public DeathDataController(DeathRegistrationRepository registrations, DeathCorrectionRepository corrections, AuditService audit) {
    this.registrations=registrations; this.corrections=corrections; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private DeathRegistration registration(UUID id) {
    return registrations.findById(id).orElseThrow(() -> BizException.notFound("registration_not_found","التسجيل غير موجود"));
  }

  public record RegBody(String deceasedRef, String place, String source, String causeCode, Boolean causeCertified) {}
  @PostMapping("/registrations")
  Map<String,Object> create(@RequestBody RegBody r) {
    var p = principal();
    var reg = registrations.save(new DeathRegistration(UUID.fromString(p.facilityId()), p.governorate(), r.deceasedRef(), r.place(),
      r.source(), r.causeCode(), Boolean.TRUE.equals(r.causeCertified())));
    return Map.of("ok",true,"registration",reg);
  }
  @PostMapping("/registrations/{id}/link-statistics")
  Map<String,Object> link(@PathVariable UUID id) {
    principal(); var reg = registration(id);
    if (!DeathRules.statisticsReady(reg.getCauseCode(), reg.isCauseCertified()))
      throw BizException.conflict("not_statistics_ready","وفاة تدخل الإحصاء الرسمي فقط بسبب مرمز معتمد ومصدق — إحصاء وفيات على أسباب غير مصدقة يضلل كل سياسة تُبنى عليه");
    reg.linkStatistics(); registrations.save(reg);
    return Map.of("ok",true,"registration",reg,"statistics_linked",true);
  }
  public record CorrectBody(String field, String newValue, String reason) {}
  @PostMapping("/registrations/{id}/correct")
  Map<String,Object> correct(@PathVariable UUID id, @RequestBody CorrectBody r) {
    var p = principal(); var reg = registration(id);
    if (!DeathRules.correctionValid(r.field(), r.newValue(), r.reason()))
      throw BizException.badRequest("correction_incomplete","تصحيح سجل وفاة يحتاج الحقل والقيمة الجديدة والسبب — التصحيح نفسه دليل");
    String old = switch (r.field()) {
      case "place" -> reg.getPlace();
      case "cause_code" -> reg.getCauseCode();
      default -> null;
    };
    if (old == null) throw BizException.badRequest("field_not_correctable","الحقل غير قابل للتصحيح من هذا المسار");
    var corr = corrections.save(new DeathCorrection(UUID.fromString(p.facilityId()), p.governorate(), id, r.field(), old, r.newValue(), r.reason()));
    if ("place".equals(r.field())) reg.correctPlace(r.newValue()); else reg.correctCause(r.newValue());
    registrations.save(reg);
    audit.record("death.correction","death_registration",id.toString(),"{}");
    return Map.of("ok",true,"correction",corr,"registration",reg,
      "note","the correction is appended to history — the record is never silently overwritten");
  }
}
