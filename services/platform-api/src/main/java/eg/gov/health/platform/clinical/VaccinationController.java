package eg.gov.health.platform.clinical;
import eg.gov.health.platform.common.*; import eg.gov.health.platform.patient.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/vaccinations")
public class VaccinationController {
  private final VaccinationRepository repo; private final PatientRepository patients; private final AuditService audit;
  public VaccinationController(VaccinationRepository repo, PatientRepository patients, AuditService audit) { this.repo=repo; this.patients=patients; this.audit=audit; }
  public record VaxRequest(UUID patientId, String vaccine, Integer doseNumber, LocalDate givenAt, String batchNumber) {}
  @PostMapping
  Map<String,Object> record(@RequestBody VaxRequest r) {
    var p = TenantContext.get();
    if (r.patientId()==null||r.vaccine()==null||r.givenAt()==null) throw BizException.badRequest("missing_field","patientId و vaccine و givenAt مطلوبة");
    var v = repo.save(new Vaccination(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.vaccine(), r.doseNumber(), r.givenAt(), r.batchNumber()));
    audit.record("vaccination.given","vaccination",v.getId().toString(),"{}");
    return Map.of("ok",true,"vaccination",v);
  }
  @GetMapping("/due/{patientId}")
  Map<String,Object> due(@PathVariable UUID patientId) {
    var patient = patients.findById(patientId).orElseThrow(() -> BizException.notFound("patient_not_found","المريض غير موجود"));
    if (patient.getBirthDate()==null) throw BizException.badRequest("missing_birth_date","تاريخ الميلاد مطلوب لحساب الاستحقاق");
    var given = new HashSet<String>(); repo.findByPatientId(patientId).forEach(v -> given.add(v.getVaccine()));
    var due = VaccinationSchedule.dueMilestones(patient.getBirthDate(), LocalDate.now(), given);
    return Map.of("ok",true,"patient_id",patientId,"due",due,"given",given);
  }
}
