#!/usr/bin/env python3
"""Batch 7 (deepening clinical core): allergies with drug-class cross-check,
problem list (ICD-10), clinical notes, discharge summaries. Flyway V8."""
import pathlib
BASE = pathlib.Path.home() / "workspace/egypt-health-v6/services/platform-api"
PKG = BASE / "src/main/java/eg/gov/health/platform"
RES = BASE / "src/main/resources"
TEST = BASE / "src/test/java/eg/gov/health/platform"
def w(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8"); print("wrote", path.name)

w(PKG/"clinical/AllergyRules.java", """package eg.gov.health.platform.clinical;
import java.util.*;
/** Allergy safety rules: drug-class membership and documented cross-
 *  reactivity. A prescription matching an active allergy (same drug, same
 *  class, or a cross-reactive class) is a HARD STOP requiring override by a
 *  senior clinician with documented reason — never a silent warning. */
public final class AllergyRules {
  private AllergyRules() {}
  public static final Map<String, Set<String>> DRUG_CLASS = Map.ofEntries(
    Map.entry("AMOXICILLIN", Set.of("penicillins")), Map.entry("AMPICILLIN", Set.of("penicillins")),
    Map.entry("PENICILLIN_G", Set.of("penicillins")),
    Map.entry("CEFTRIAXONE", Set.of("cephalosporins")), Map.entry("CEFUROXIME", Set.of("cephalosporins")),
    Map.entry("CEPHALEXIN", Set.of("cephalosporins")),
    Map.entry("SULFAMETHOXAZOLE", Set.of("sulfonamides")), Map.entry("ASPIRIN", Set.of("nsaids")),
    Map.entry("IBUPROFEN", Set.of("nsaids")), Map.entry("DICLOFENAC", Set.of("nsaids")));
  public static final Map<String, Set<String>> CROSS_REACTIVE = Map.of(
    "penicillins", Set.of("cephalosporins"),
    "cephalosporins", Set.of("penicillins"));
  public record AllergyView(String allergen, String allergenClass, String severity, String status) {}
  public static Optional<String> conflict(String drugCode, Collection<AllergyView> activeAllergies) {
    if (drugCode == null) return Optional.empty();
    String drug = drugCode.toUpperCase();
    Set<String> drugClasses = DRUG_CLASS.getOrDefault(drug, Set.of());
    for (var a : activeAllergies) {
      if (!"active".equals(a.status())) continue;
      if (a.allergen() != null && a.allergen().equalsIgnoreCase(drug))
        return Optional.of("نفس الدواء: " + drug + " (خطورة " + a.severity() + ")");
      if (a.allergenClass() != null) {
        if (drugClasses.contains(a.allergenClass()))
          return Optional.of("نفس الفئة الدوائية: " + a.allergenClass());
        var cross = CROSS_REACTIVE.getOrDefault(a.allergenClass(), Set.of());
        for (String dc : drugClasses) if (cross.contains(dc))
          return Optional.of("تفاعل تصالبي موثق: حساسية " + a.allergenClass() + " مع فئة " + dc);
      }
    }
    return Optional.empty();
  }
}""")
w(PKG/"clinical/Allergy.java", """package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "allergies")
public class Allergy {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String allergen;
  @Column(name="allergen_class") private String allergenClass;
  @Column(nullable=false) private String severity = "moderate"; // mild, moderate, severe, life_threatening
  private String reaction;
  @Column(nullable=false) private String status = "active"; // active, resolved, entered_in_error
  @Column(name="recorded_by") private String recordedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Allergy() {}
  public Allergy(UUID facilityId, String governorate, UUID patientId, String allergen, String allergenClass, String severity, String reaction, String recordedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.allergen=allergen;
    this.allergenClass=allergenClass; this.severity=severity==null?"moderate":severity; this.reaction=reaction; this.recordedBy=recordedBy;
  }
  public UUID getId(){return id;} public UUID getPatientId(){return patientId;}
  public AllergyRules.AllergyView toView(){ return new AllergyRules.AllergyView(allergen, allergenClass, severity, status); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"clinical/AllergyRepository.java", """package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AllergyRepository extends JpaRepository<Allergy, UUID> {
  List<Allergy> findByPatientIdAndStatus(UUID patientId, String status);
  List<Allergy> findByPatientId(UUID patientId);
}""")
w(PKG/"clinical/ProblemListItem.java", """package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "problem_list")
public class ProblemListItem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="icd10_code", nullable=false) private String icd10Code;
  @Column(nullable=false) private String description;
  @Column(name="onset_date") private LocalDate onsetDate;
  @Column(nullable=false) private String status = "active"; // active, resolved, inactive
  @Column(name="recorded_by") private String recordedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ProblemListItem() {}
  public ProblemListItem(UUID facilityId, String governorate, UUID patientId, String icd10Code, String description, LocalDate onsetDate, String recordedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.icd10Code=icd10Code;
    this.description=description; this.onsetDate=onsetDate; this.recordedBy=recordedBy;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getPatientId(){return patientId;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"clinical/ProblemListRepository.java", """package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ProblemListRepository extends JpaRepository<ProblemListItem, UUID> {
  List<ProblemListItem> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}""")
w(PKG/"clinical/ClinicalNote.java", """package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "clinical_notes")
public class ClinicalNote {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(nullable=false) private String noteType = "progress"; // admission, progress, procedure, discharge, nursing
  @Column(name="subjective") private String subjective;
  @Column(name="objective") private String objective;
  @Column(name="assessment") private String assessment;
  @Column(name="plan") private String plan;
  @Column(name="author_id") private String authorId;
  @Column(nullable=false) private String status = "draft"; // draft, signed, amended
  @Column(name="signed_at") private OffsetDateTime signedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ClinicalNote() {}
  public ClinicalNote(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String noteType, String s, String o, String a, String p, String authorId) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.noteType=noteType==null?"progress":noteType; this.subjective=s; this.objective=o; this.assessment=a; this.plan=p; this.authorId=authorId;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getPatientId(){return patientId;}
  public void sign(){this.status="signed"; this.signedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now();}
  public void amend(String s, String o, String a, String p){this.subjective=s; this.objective=o; this.assessment=a; this.plan=p; this.status="amended"; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"clinical/ClinicalNoteRepository.java", """package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, UUID> {
  List<ClinicalNote> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
  List<ClinicalNote> findByEncounterIdOrderByCreatedAtAsc(UUID encounterId);
}""")
w(PKG/"clinical/ClinicalDeepeningController.java", """package eg.gov.health.platform.clinical;
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
}""")
print("batch7 java done")

v8 = """-- V8 — Batch 7 (clinical deepening): allergies, problem list, clinical notes.
CREATE TABLE IF NOT EXISTS allergies (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  allergen TEXT NOT NULL, allergen_class TEXT,
  severity TEXT NOT NULL DEFAULT 'moderate' CHECK (severity IN ('mild','moderate','severe','life_threatening')),
  reaction TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','resolved','entered_in_error')),
  recorded_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_allergies_patient ON allergies (patient_id, status);
CREATE TABLE IF NOT EXISTS problem_list (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  icd10_code TEXT NOT NULL, description TEXT NOT NULL, onset_date DATE,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','resolved','inactive')),
  recorded_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_problems_patient ON problem_list (patient_id, status);
CREATE TABLE IF NOT EXISTS clinical_notes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), encounter_id UUID REFERENCES encounters(id),
  note_type TEXT NOT NULL DEFAULT 'progress',
  subjective TEXT, objective TEXT, assessment TEXT, plan TEXT,
  author_id TEXT,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','signed','amended')),
  signed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_notes_patient ON clinical_notes (patient_id, created_at);
GRANT SELECT, INSERT, UPDATE, DELETE ON allergies, problem_list, clinical_notes TO app_user;
"""
w(RES/"db/migration/V8__allergies_problems_notes.sql", v8)
w(TEST/"clinical/AllergyRulesTest.java", """package eg.gov.health.platform.clinical;
import org.junit.jupiter.api.Test; import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class AllergyRulesTest {
  AllergyRules.AllergyView pen() { return new AllergyRules.AllergyView("AMOXICILLIN","penicillins","severe","active"); }
  @Test void sameDrugConflicts() { assertTrue(AllergyRules.conflict("AMOXICILLIN", List.of(pen())).isPresent()); }
  @Test void sameClassConflicts() { assertTrue(AllergyRules.conflict("AMPICILLIN", List.of(pen())).isPresent()); }
  @Test void crossReactiveConflicts() { assertTrue(AllergyRules.conflict("CEFTRIAXONE", List.of(pen())).isPresent()); }
  @Test void unrelatedDrugClear() { assertTrue(AllergyRules.conflict("PARACETAMOL", List.of(pen())).isEmpty()); }
  @Test void resolvedAllergyIgnored() {
    var resolved = new AllergyRules.AllergyView("AMOXICILLIN","penicillins","severe","resolved");
    assertTrue(AllergyRules.conflict("AMOXICILLIN", List.of(resolved)).isEmpty());
  }
}""")
print("batch7 migration+tests done")
