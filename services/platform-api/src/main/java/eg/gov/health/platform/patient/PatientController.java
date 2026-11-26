package eg.gov.health.platform.patient;
import eg.gov.health.platform.common.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/patients")
public class PatientController {
  private final PatientRepository repo; private final AuditService audit;
  public PatientController(PatientRepository repo, AuditService audit) { this.repo = repo; this.audit = audit; }
  public record PatientRequest(String nationalId, @NotBlank String fullName, LocalDate birthDate, String gender,
                               String phone, String address, String identityStatus) {}
  @PostMapping
  Map<String, Object> register(@RequestBody @jakarta.validation.Valid PatientRequest r) {
    var p = TenantContext.get();
    UUID fac = p != null && p.facilityId() != null ? UUID.fromString(p.facilityId()) : null;
    if (r.nationalId() != null && !r.nationalId().isBlank()) {
      var existing = repo.findByNationalId(r.nationalId());
      if (existing.isPresent())
        throw BizException.conflict("duplicate_national_id", "رقم قومي مسجل مسبقًا — لا يُنشأ معرف مكرر (دورة 217)");
    }
    var saved = repo.save(new Patient(fac, p == null ? null : p.governorate(), r.nationalId(), r.fullName(),
      r.birthDate(), r.gender(), r.phone(), r.address(), r.identityStatus()));
    // Explainable duplicate candidates for human review (never auto-merge).
    var candidates = new java.util.ArrayList<Map<String, Object>>();
    if (r.birthDate() != null) {
      for (var other : repo.findByFullNameAndBirthDate(r.fullName(), r.birthDate())) {
        if (other.getId().equals(saved.getId())) continue;
        var c = DuplicateDetector.evaluate(r.fullName(), r.birthDate(), r.phone(), other.getFullName(), other.getBirthDate(), other.getPhone());
        if (DuplicateDetector.isReviewCandidate(c))
          candidates.add(Map.of("patient_id", other.getId(), "reason", c.reason(), "score", c.score()));
      }
    }
    audit.record("patient.registered", "patient", saved.getId().toString(), "{}");
    return Map.of("ok", true, "patient", saved, "duplicate_candidates", candidates);
  }
  @GetMapping("/{id}")
  Patient get(@PathVariable UUID id) {
    return repo.findById(id).orElseThrow(() -> BizException.notFound("patient_not_found", "المريض غير موجود"));
  }
  @GetMapping
  Map<String, Object> list(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    var p = TenantContext.get();
    var rows = (p != null && p.facilityId() != null) ? repo.findByFacilityId(UUID.fromString(p.facilityId())) : repo.findAll();
    return Paging.page(rows, "patients", page, size);
  }
  /** Merge (cycle 217): identity-officer only, fully audited, reversible via
   *  the preserved merged_into link; source record is never deleted. */
  @PostMapping("/{sourceId}/merge-into/{targetId}")
  @PreAuthorize("hasAnyRole('national_admin','governorate_admin')")
  Map<String, Object> merge(@PathVariable UUID sourceId, @PathVariable UUID targetId, @RequestBody Map<String, String> body) {
    if (sourceId.equals(targetId)) throw BizException.badRequest("same_patient", "لا يمكن دمج المريض في نفسه");
    if (body.getOrDefault("reason", "").isBlank()) throw BizException.badRequest("reason_required", "سبب الدمج مطلوب وموثق");
    var source = get(sourceId); var target = get(targetId);
    if (!"active".equals(source.getStatus())) throw BizException.conflict("already_merged", "المصدر مدمج مسبقًا");
    source.markMergedInto(targetId); repo.save(source);
    audit.record("patient.merged", "patient", sourceId.toString(),
      "{\"target\":\"" + targetId + "\",\"reason\":\"" + body.get("reason").replace("\"", "'") + "\"}");
    return Map.of("ok", true, "source", sourceId, "target", targetId, "status", "merged");
  }
}
