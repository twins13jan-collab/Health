package eg.gov.health.platform.childhealth;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Child health, immunization & growth (module 96). The child
 *  record, growth, nutrition and development are managed;
 *  immunization schedules, late doses and catch-up run; the
 *  vaccine cold chain and batches are governed; adverse events
 *  following immunization are reported and investigated; early
 *  detection of disabilities and developmental disorders,
 *  nutrition programmes and malnutrition control run; and
 *  children with chronic diseases and complex needs are
 *  followed. */
@RestController @RequestMapping("/api/child-health")
public class ChildHealthController {
  private final ChildRecordRepository children; private final ImmunizationDoseRepository doses;
  private final AefiReportRepository aefis; private final AuditService audit;
  public ChildHealthController(ChildRecordRepository children, ImmunizationDoseRepository doses,
                               AefiReportRepository aefis, AuditService audit) {
    this.children=children; this.doses=doses; this.aefis=aefis; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ChildRecord child(UUID id) {
    return children.findById(id).orElseThrow(() -> BizException.notFound("child_not_found","الطفل غير موجود"));
  }

  public record ChildBody(String childRef, LocalDate birthDate) {}
  @PostMapping("/children")
  Map<String,Object> create(@RequestBody ChildBody r) {
    var p = principal();
    var c = children.save(new ChildRecord(UUID.fromString(p.facilityId()), p.governorate(), r.childRef(), r.birthDate()));
    return Map.of("ok",true,"child",c);
  }
  public record DoseBody(String vaccine, String batchRef, Boolean coldChainOk, LocalDate givenDate) {}
  @PostMapping("/children/{id}/doses")
  Map<String,Object> dose(@PathVariable UUID id, @RequestBody DoseBody r) {
    var p = principal(); child(id);
    // AEFI gate: an uninvestigated AEFI on the same vaccine blocks the next dose.
    for (var prev : doses.findByChildId(id)) {
      if (prev.getVaccine().equals(r.vaccine())) {
        for (var a : aefis.findByDoseId(prev.getId())) {
          if (!ChildHealthRules.nextDosePermitted(true, a.isInvestigated()))
            throw BizException.conflict("aefi_uninvestigated","حدث تالٍ للتطعيم غير محقق فيه يمنع الجرعة التالية من اللقاح نفسه لهذا الطفل");
        }
      }
    }
    var d = doses.save(new ImmunizationDose(UUID.fromString(p.facilityId()), p.governorate(), id, r.vaccine(), r.batchRef(),
      Boolean.TRUE.equals(r.coldChainOk()), r.givenDate()));
    if (!d.isValid()) audit.record("childhealth.dose.invalid_coldchain","immunization_dose",d.getId().toString(),"{}");
    return Map.of("ok",true,"dose",d,"valid",d.isValid(),
      "note", d.isValid() ? "counted" : "a dose from a broken cold chain is NOT a dose — recorded invalid, to be repeated");
  }
  public record AefiBody(String description) {}
  @PostMapping("/doses/{doseId}/aefi")
  Map<String,Object> aefi(@PathVariable UUID doseId, @RequestBody AefiBody r) {
    var p = principal();
    doses.findById(doseId).orElseThrow(() -> BizException.notFound("dose_not_found","الجرعة غير موجودة"));
    var a = aefis.save(new AefiReport(UUID.fromString(p.facilityId()), p.governorate(), doseId, r.description(), false));
    return Map.of("ok",true,"aefi",a);
  }
  @PostMapping("/aefi/{id}/investigate")
  Map<String,Object> investigate(@PathVariable UUID id) {
    principal();
    var a = aefis.findById(id).orElseThrow(() -> BizException.notFound("aefi_not_found","البلاغ غير موجود"));
    a.investigate(); aefis.save(a);
    audit.record("childhealth.aefi.investigated","aefi_report",id.toString(),"{}");
    return Map.of("ok",true,"aefi",a);
  }
}
