package eg.gov.health.platform.governance;
import eg.gov.health.platform.common.*; import eg.gov.health.platform.patient.*;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/death-registry")
public class DeathRegistryController {
  private final DeathRecordRepository repo; private final PatientRepository patients; private final AuditService audit;
  public DeathRegistryController(DeathRecordRepository repo, PatientRepository patients, AuditService audit) { this.repo=repo; this.patients=patients; this.audit=audit; }
  public record DeathRequest(UUID patientId, OffsetDateTime diedAt, String causeIcd10, String causeDescription) {}
  @PostMapping @Transactional
  Map<String,Object> certify(@RequestBody DeathRequest r) {
    var p = TenantContext.get();
    if (r.patientId()==null||r.diedAt()==null||r.causeIcd10()==null) throw BizException.badRequest("missing_field","بيانات الشهادة ناقصة");
    if (repo.findByPatientId(r.patientId()).isPresent()) throw BizException.conflict("already_certified","شهادة وفاة قائمة لهذا المريض — التعديل عبر التصحيح الموثق فقط");
    var patient = patients.findById(r.patientId()).orElseThrow(() -> BizException.notFound("patient_not_found","المريض غير موجود"));
    var rec = repo.save(new DeathRecord(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.diedAt(), r.causeIcd10(), r.causeDescription(), p==null?null:p.userId()));
    patient.setStatus("deceased"); patients.save(patient); // MPI status follows the certificate — one fact, one source
    audit.record("death.certified","death_record",rec.getId().toString(),"{}");
    return Map.of("ok",true,"record",rec);
  }
  @PostMapping("/{id}/correct")
  Map<String,Object> correct(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var rec = repo.findById(id).orElseThrow(() -> BizException.notFound("record_not_found","السجل غير موجود"));
    if (body.getOrDefault("reason","").isBlank()) throw BizException.badRequest("reason_required","تصحيح سبب الوفاة بسبب موثق إلزاميًا");
    rec.correct(body.getOrDefault("cause_icd10", ""), body.getOrDefault("cause_description","")); repo.save(rec);
    audit.record("death.corrected","death_record",id.toString(),"{}");
    return Map.of("ok",true,"record",rec);
  }
  @GetMapping
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByDiedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"records",rows,"count",rows.size());
  }
}
