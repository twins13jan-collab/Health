package eg.gov.health.platform.clinical;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/clinical")
public class ClinicalDeepeningController {
  private final AllergyRepository allergies; private final ProblemListRepository problems;
  private final ClinicalNoteRepository notes; private final AuditService audit;
  public ClinicalDeepeningController(AllergyRepository allergies, ProblemListRepository problems, ClinicalNoteRepository notes, AuditService audit) {
    this.allergies=allergies; this.problems=problems; this.notes=notes; this.audit=audit;
  }
  public record AllergyRequest(UUID patientId, String allergen, String allergenClass, String severity, String reaction) {}
  @PostMapping("/allergies")
  Map<String,Object> addAllergy(@RequestBody AllergyRequest r) {
    var p = TenantContext.get();
    if (r.patientId()==null||r.allergen()==null) throw BizException.badRequest("missing_field","patientId و allergen مطلوبان");
    var a = allergies.save(new Allergy(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.allergen(), r.allergenClass(), r.severity(), r.reaction(), p==null?null:p.userId()));
    audit.record("clinical.allergy.recorded","allergy",a.getId().toString(),"{}");
    return Map.of("ok",true,"allergy",a);
  }
  @GetMapping("/allergies/check")
  Map<String,Object> checkAllergy(@RequestParam UUID patientId, @RequestParam String drugCode) {
    var active = allergies.findByPatientIdAndStatus(patientId, "active").stream().map(Allergy::toView).toList();
    var conflict = AllergyRules.conflict(drugCode, active);
    audit.record(conflict.isPresent()?"clinical.allergy.conflict":"clinical.allergy.clear","patient",patientId.toString(),"{}");
    var out = new HashMap<String,Object>(); out.put("ok",true); out.put("drug",drugCode);
    out.put("hard_stop", conflict.isPresent()); conflict.ifPresent(c -> out.put("conflict", c));
    return out;
  }
  @GetMapping("/allergies")
  Map<String,Object> listAllergies(@RequestParam UUID patientId) {
    var rows = allergies.findByPatientId(patientId);
    return Map.of("ok",true,"allergies",rows,"count",rows.size());
  }
  public record ProblemRequest(UUID patientId, String icd10Code, String description, LocalDate onsetDate) {}
  @PostMapping("/problems")
  Map<String,Object> addProblem(@RequestBody ProblemRequest r) {
    var p = TenantContext.get();
    if (r.patientId()==null||r.icd10Code()==null||r.description()==null) throw BizException.badRequest("missing_field","بيانات المشكلة ناقصة");
    boolean dup = problems.findByPatientIdOrderByCreatedAtDesc(r.patientId()).stream()
      .anyMatch(x -> "active".equals(x.getStatus()) && r.icd10Code().equalsIgnoreCase(getIcd(x)));
    var item = problems.save(new ProblemListItem(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.icd10Code(), r.description(), r.onsetDate(), p==null?null:p.userId()));
    var out = new HashMap<String,Object>(); out.put("ok",true); out.put("problem",item);
    if (dup) out.put("warning","نفس رمز ICD-10 نشط بالفعل في قائمة المشكلات — راجع الازدواج");
    return out;
  }
  private String getIcd(ProblemListItem x) {
    try { var f = ProblemListItem.class.getDeclaredField("icd10Code"); f.setAccessible(true); return (String) f.get(x); }
    catch (Exception e) { return ""; }
  }
  @PostMapping("/problems/{id}/resolve")
  Map<String,Object> resolveProblem(@PathVariable UUID id) {
    var item = problems.findById(id).orElseThrow(() -> BizException.notFound("problem_not_found","المشكلة غير موجودة"));
    item.setStatus("resolved"); problems.save(item);
    return Map.of("ok",true,"problem",item);
  }
  @GetMapping("/problems")
  Map<String,Object> listProblems(@RequestParam UUID patientId) {
    var rows = problems.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"problems",rows,"count",rows.size());
  }
  public record NoteRequest(UUID patientId, UUID encounterId, String noteType, String subjective, String objective, String assessment, String plan) {}
  @PostMapping("/notes")
  Map<String,Object> addNote(@RequestBody NoteRequest r) {
    var p = TenantContext.get();
    if (r.patientId()==null) throw BizException.badRequest("missing_field","patientId مطلوب");
    var n = notes.save(new ClinicalNote(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.encounterId(), r.noteType(), r.subjective(), r.objective(), r.assessment(), r.plan(), p==null?null:p.userId()));
    return Map.of("ok",true,"note",n);
  }
  @PostMapping("/notes/{id}/sign")
  Map<String,Object> signNote(@PathVariable UUID id) {
    var n = notes.findById(id).orElseThrow(() -> BizException.notFound("note_not_found","الملاحظة غير موجودة"));
    if ("signed".equals(n.getStatus())) throw BizException.conflict("already_signed","موقعة بالفعل — التعديل عبر إصدار تعديل موثق (amend) لا إعادة توقيع");
    n.sign(); notes.save(n);
    audit.record("clinical.note.signed","clinical_note",id.toString(),"{}");
    return Map.of("ok",true,"note",n);
  }
  @GetMapping("/notes")
  Map<String,Object> listNotes(@RequestParam UUID patientId) {
    var rows = notes.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"notes",rows,"count",rows.size());
  }
}
