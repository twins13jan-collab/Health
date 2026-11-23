#!/usr/bin/env python3
"""Batch 6: dialysis (242), oncology chemo dosing, telemedicine, death
registry, medical documents & retention, procurement, asset maintenance."""
import pathlib
BASE = pathlib.Path.home() / "workspace/egypt-health-v6/services/platform-api"
PKG = BASE / "src/main/java/eg/gov/health/platform"
RES = BASE / "src/main/resources"
TEST = BASE / "src/test/java/eg/gov/health/platform"
def w(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8"); print("wrote", path.name)

# ---------------- dialysis (cycle 242): machine-slot scheduling
w(PKG/"programs/DialysisSession.java", """package eg.gov.health.platform.programs;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "dialysis_sessions")
public class DialysisSession {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="machine_code", nullable=false) private String machineCode;
  @Column(name="starts_at", nullable=false) private OffsetDateTime startsAt;
  @Column(name="ends_at", nullable=false) private OffsetDateTime endsAt;
  @Column(name="dry_weight_kg") private Double dryWeightKg;
  @Column(name="ultrafiltration_ml") private Integer ultrafiltrationMl;
  @Column(nullable=false) private String status = "scheduled"; // scheduled, in_session, completed, missed, cancelled
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DialysisSession() {}
  public DialysisSession(UUID facilityId, String governorate, UUID patientId, String machineCode, OffsetDateTime startsAt, OffsetDateTime endsAt, Double dryWeightKg, Integer uf) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.machineCode=machineCode;
    this.startsAt=startsAt; this.endsAt=endsAt; this.dryWeightKg=dryWeightKg; this.ultrafiltrationMl=uf;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public OffsetDateTime getStartsAt(){return startsAt;} public OffsetDateTime getEndsAt(){return endsAt;}
  public String getMachineCode(){return machineCode;}
  public boolean overlaps(DialysisSession o) {
    return machineCode.equals(o.machineCode) && !"cancelled".equals(o.status) && startsAt.isBefore(o.endsAt) && o.startsAt.isBefore(endsAt);
  }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"programs/DialysisSessionRepository.java", """package eg.gov.health.platform.programs;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DialysisSessionRepository extends JpaRepository<DialysisSession, UUID> {
  List<DialysisSession> findByFacilityIdAndMachineCode(UUID facilityId, String machineCode);
  List<DialysisSession> findByPatientIdOrderByStartsAtAsc(UUID patientId);
}""")
w(PKG/"programs/DialysisController.java", """package eg.gov.health.platform.programs;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/dialysis")
public class DialysisController {
  private final DialysisSessionRepository repo; private final AuditService audit;
  public DialysisController(DialysisSessionRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record SessionRequest(UUID patientId, String machineCode, OffsetDateTime startsAt, OffsetDateTime endsAt, Double dryWeightKg, Integer ultrafiltrationMl) {}
  @PostMapping("/sessions")
  Map<String,Object> schedule(@RequestBody SessionRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.machineCode()==null||r.startsAt()==null||r.endsAt()==null||!r.endsAt().isAfter(r.startsAt()))
      throw BizException.badRequest("invalid_session","بيانات جلسة غير صحيحة");
    var candidate = new DialysisSession(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.machineCode(), r.startsAt(), r.endsAt(), r.dryWeightKg(), r.ultrafiltrationMl());
    boolean clash = repo.findByFacilityIdAndMachineCode(candidate.getFacilityId(), r.machineCode()).stream().anyMatch(candidate::overlaps);
    if (clash) throw BizException.conflict("machine_busy","الماكينة محجوزة في هذه الفترة — لا حجز مزدوج لماكينات الغسيل (دورة 242)");
    var s = repo.save(candidate);
    audit.record("dialysis.session.scheduled","dialysis_session",s.getId().toString(),"{}");
    return Map.of("ok",true,"session",s);
  }
  @PostMapping("/sessions/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var s = repo.findById(id).orElseThrow(() -> BizException.notFound("session_not_found","الجلسة غير موجودة"));
    String to = body.getOrDefault("status","");
    Map<String,List<String>> flow = Map.of(
      "scheduled", List.of("in_session","missed","cancelled"), "in_session", List.of("completed"),
      "completed", List.of(), "missed", List.of(), "cancelled", List.of());
    if (!flow.getOrDefault(s.getStatus(), List.of()).contains(to))
      throw BizException.conflict("illegal_transition","انتقال غير مسموح: "+s.getStatus()+" -> "+to);
    s.setStatus(to); repo.save(s);
    audit.record("dialysis.session."+to,"dialysis_session",id.toString(),"{}");
    return Map.of("ok",true,"session",s);
  }
  @GetMapping("/sessions")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = repo.findByPatientIdOrderByStartsAtAsc(patientId);
    return Map.of("ok",true,"sessions",rows,"count",rows.size());
  }
}""")

# ---------------- oncology: BSA + dose computation (pure, safety-critical)
w(PKG/"programs/ChemoDosing.java", """package eg.gov.health.platform.programs;
import java.math.*;
/** Chemotherapy dose computation (oncology program): body-surface area by
 *  the Mosteller formula; dose = BSA x protocol mg/m2, with renal dose
 *  adjustment bands by creatinine clearance. Computed values are advisory
 *  to the treating oncologist and the verifying pharmacist — the system
 *  never administers; it computes, checks caps, and records. */
public final class ChemoDosing {
  private ChemoDosing() {}
  public static double bsaMosteller(double heightCm, double weightKg) {
    if (heightCm <= 0 || weightKg <= 0) throw new IllegalArgumentException("height/weight must be positive");
    return Math.sqrt(heightCm * weightKg / 3600.0);
  }
  public static BigDecimal doseMg(double bsa, BigDecimal mgPerM2) {
    return mgPerM2.multiply(BigDecimal.valueOf(bsa)).setScale(1, RoundingMode.HALF_UP);
  }
  /** Dose fraction by creatinine clearance (ml/min): >=60 full, 40-59 75%, 25-39 50%, <25 contraindicated review. */
  public static BigDecimal renalAdjustmentPct(int creatinineClearance) {
    if (creatinineClearance >= 60) return new BigDecimal("100");
    if (creatinineClearance >= 40) return new BigDecimal("75");
    if (creatinineClearance >= 25) return new BigDecimal("50");
    return BigDecimal.ZERO; // below 25: no automatic dose — specialist review mandatory
  }
  public static BigDecimal adjustedDoseMg(double bsa, BigDecimal mgPerM2, int crCl) {
    var pct = renalAdjustmentPct(crCl);
    if (pct.signum() == 0) return BigDecimal.ZERO;
    return doseMg(bsa, mgPerM2).multiply(pct).divide(new BigDecimal("100"), 1, RoundingMode.HALF_UP);
  }
}""")
w(PKG/"programs/ChemoCycle.java", """package eg.gov.health.platform.programs;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.*; import java.util.UUID;
@Entity @Table(name = "chemo_cycles")
public class ChemoCycle {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String protocol;
  @Column(name="drug_code", nullable=false) private String drugCode;
  @Column(name="cycle_number", nullable=false) private int cycleNumber;
  @Column(name="bsa", nullable=false) private Double bsa;
  @Column(name="dose_mg", nullable=false) private BigDecimal doseMg;
  @Column(name="scheduled_date") private LocalDate scheduledDate;
  @Column(nullable=false) private String status = "planned"; // planned, verified, administered, held, cancelled
  @Column(name="verified_by") private String verifiedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ChemoCycle() {}
  public ChemoCycle(UUID facilityId, String governorate, UUID patientId, String protocol, String drugCode, int cycleNumber, Double bsa, BigDecimal doseMg, LocalDate scheduledDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.protocol=protocol;
    this.drugCode=drugCode; this.cycleNumber=cycleNumber; this.bsa=bsa; this.doseMg=doseMg; this.scheduledDate=scheduledDate;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public BigDecimal getDoseMg(){return doseMg;}
  public void verify(String by){this.verifiedBy=by; this.status="verified"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"programs/ChemoCycleRepository.java", """package eg.gov.health.platform.programs;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ChemoCycleRepository extends JpaRepository<ChemoCycle, UUID> {
  List<ChemoCycle> findByPatientIdOrderByCycleNumberAsc(UUID patientId);
}""")
w(PKG/"programs/OncologyController.java", """package eg.gov.health.platform.programs;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/oncology")
public class OncologyController {
  private final ChemoCycleRepository repo; private final AuditService audit;
  public OncologyController(ChemoCycleRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record CycleRequest(UUID patientId, String protocol, String drugCode, Integer cycleNumber,
                             Double heightCm, Double weightKg, BigDecimal mgPerM2, Integer creatinineClearance, LocalDate scheduledDate) {}
  @PostMapping("/cycles")
  Map<String,Object> plan(@RequestBody CycleRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.drugCode()==null||r.heightCm()==null||r.weightKg()==null||r.mgPerM2()==null||r.creatinineClearance()==null)
      throw BizException.badRequest("missing_field","بيانات الجرعة ناقصة");
    double bsa = ChemoDosing.bsaMosteller(r.heightCm(), r.weightKg());
    var pct = ChemoDosing.renalAdjustmentPct(r.creatinineClearance());
    if (pct.signum()==0)
      throw BizException.conflict("renal_review_required","تصفية كرياتينين أقل من 25 — لا جرعة آلية؛ مراجعة استشاري أورام وكلى إلزامية");
    var dose = ChemoDosing.adjustedDoseMg(bsa, r.mgPerM2(), r.creatinineClearance());
    var c = repo.save(new ChemoCycle(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.protocol(), r.drugCode(),
      r.cycleNumber()==null?1:r.cycleNumber(), Math.round(bsa*100.0)/100.0, dose, r.scheduledDate()));
    audit.record("oncology.cycle.planned","chemo_cycle",c.getId().toString(),"{}");
    return Map.of("ok",true,"cycle",c,"bsa",Math.round(bsa*100.0)/100.0,"renal_adjustment_pct",pct,"dose_mg",dose,
      "note","جرعة محسوبة استشاريًا — تتطلب تحقق صيدلي سريري مستقل قبل التحضير");
  }
  @PostMapping("/cycles/{id}/verify")
  Map<String,Object> verify(@PathVariable UUID id) {
    var p = TenantContext.get();
    var c = repo.findById(id).orElseThrow(() -> BizException.notFound("cycle_not_found","الدورة غير موجودة"));
    if (!"planned".equals(c.getStatus())) throw BizException.conflict("not_planned","التحقق من حالة التخطيط فقط");
    c.verify(p==null?null:p.userId()); repo.save(c);
    audit.record("oncology.cycle.verified","chemo_cycle",id.toString(),"{}");
    return Map.of("ok",true,"cycle",c);
  }
  @PostMapping("/cycles/{id}/administer")
  Map<String,Object> administer(@PathVariable UUID id) {
    var c = repo.findById(id).orElseThrow(() -> BizException.notFound("cycle_not_found","الدورة غير موجودة"));
    if (!"verified".equals(c.getStatus())) throw BizException.conflict("not_verified","لا إعطاء قبل تحقق صيدلي موثق — قاعدة سلامة مطلقة");
    c.setStatus("administered"); repo.save(c);
    audit.record("oncology.cycle.administered","chemo_cycle",id.toString(),"{}");
    return Map.of("ok",true,"cycle",c);
  }
  @GetMapping("/cycles")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = repo.findByPatientIdOrderByCycleNumberAsc(patientId);
    return Map.of("ok",true,"cycles",rows,"count",rows.size());
  }
}""")

# ---------------- telemedicine
w(PKG/"clinical/Teleconsultation.java", """package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "teleconsultations")
public class Teleconsultation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="consultant_name", nullable=false) private String consultantName;
  @Column(name="specialty") private String specialty;
  @Column(name="scheduled_at", nullable=false) private OffsetDateTime scheduledAt;
  @Column(nullable=false) private String status = "scheduled"; // scheduled, in_session, completed, no_show, cancelled, escalated_to_in_person
  @Column(name="session_notes") private String sessionNotes;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Teleconsultation() {}
  public Teleconsultation(UUID facilityId, String governorate, UUID patientId, String consultantName, String specialty, OffsetDateTime scheduledAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.consultantName=consultantName;
    this.specialty=specialty; this.scheduledAt=scheduledAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void complete(String notes){this.sessionNotes=notes; this.status="completed"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"clinical/TeleconsultationRepository.java", """package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface TeleconsultationRepository extends JpaRepository<Teleconsultation, UUID> {
  List<Teleconsultation> findByPatientIdOrderByScheduledAtAsc(UUID patientId);
}""")
w(PKG/"clinical/TelemedicineController.java", """package eg.gov.health.platform.clinical;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/telemedicine")
public class TelemedicineController {
  private final TeleconsultationRepository repo; private final AuditService audit;
  public TelemedicineController(TeleconsultationRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record TeleRequest(UUID patientId, String consultantName, String specialty, OffsetDateTime scheduledAt) {}
  @PostMapping("/consultations")
  Map<String,Object> book(@RequestBody TeleRequest r) {
    var p = TenantContext.get();
    if (r.patientId()==null||r.consultantName()==null||r.scheduledAt()==null) throw BizException.badRequest("missing_field","بيانات الاستشارة ناقصة");
    var t = repo.save(new Teleconsultation(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.consultantName(), r.specialty(), r.scheduledAt()));
    audit.record("telemedicine.booked","teleconsultation",t.getId().toString(),"{}");
    return Map.of("ok",true,"consultation",t);
  }
  @PostMapping("/consultations/{id}/complete")
  Map<String,Object> complete(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var t = repo.findById(id).orElseThrow(() -> BizException.notFound("consultation_not_found","الاستشارة غير موجودة"));
    if (!"in_session".equals(t.getStatus()) && !"scheduled".equals(t.getStatus())) throw BizException.conflict("bad_state","الإكمال من جلسة قائمة فقط");
    if (body.getOrDefault("notes","").isBlank()) throw BizException.badRequest("notes_required","ملاحظات الجلسة جزء من السجل الطبي — إلزامية");
    t.complete(body.get("notes")); repo.save(t);
    audit.record("telemedicine.completed","teleconsultation",id.toString(),"{}");
    return Map.of("ok",true,"consultation",t);
  }
  @PostMapping("/consultations/{id}/escalate")
  Map<String,Object> escalate(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var t = repo.findById(id).orElseThrow(() -> BizException.notFound("consultation_not_found","الاستشارة غير موجودة"));
    if (body.getOrDefault("reason","").isBlank()) throw BizException.badRequest("reason_required","سبب التحويل لحضوري موثق إلزاميًا");
    t.setStatus("escalated_to_in_person"); repo.save(t);
    audit.record("telemedicine.escalated","teleconsultation",id.toString(),"{}");
    return Map.of("ok",true,"consultation",t);
  }
  @GetMapping("/consultations")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = repo.findByPatientIdOrderByScheduledAtAsc(patientId);
    return Map.of("ok",true,"consultations",rows,"count",rows.size());
  }
}""")

# ---------------- death registry
w(PKG/"governance/DeathRecord.java", """package eg.gov.health.platform.governance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "death_records")
public class DeathRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="died_at", nullable=false) private OffsetDateTime diedAt;
  @Column(name="cause_icd10", nullable=false) private String causeIcd10;
  @Column(name="cause_description") private String causeDescription;
  @Column(name="certified_by", nullable=false) private String certifiedBy;
  @Column(nullable=false) private String status = "certified"; // certified, under_review, corrected
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DeathRecord() {}
  public DeathRecord(UUID facilityId, String governorate, UUID patientId, OffsetDateTime diedAt, String causeIcd10, String causeDescription, String certifiedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.diedAt=diedAt;
    this.causeIcd10=causeIcd10; this.causeDescription=causeDescription; this.certifiedBy=certifiedBy;
  }
  public UUID getId(){return id;} public UUID getPatientId(){return patientId;} public String getStatus(){return status;}
  public void correct(String icd10, String desc){this.causeIcd10=icd10; this.causeDescription=desc; this.status="corrected"; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"governance/DeathRecordRepository.java", """package eg.gov.health.platform.governance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DeathRecordRepository extends JpaRepository<DeathRecord, UUID> {
  Optional<DeathRecord> findByPatientId(UUID patientId);
  List<DeathRecord> findByFacilityIdOrderByDiedAtDesc(UUID facilityId);
}""")
w(PKG/"governance/DeathRegistryController.java", """package eg.gov.health.platform.governance;
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
}""")

# ---------------- medical documents + retention
w(PKG/"governance/RetentionRules.java", """package eg.gov.health.platform.governance;
import java.time.LocalDate;
/** Medical-record retention (documents module): records are retained for
 *  the national minimum period by category; disposal before the retention
 *  date is prohibited, and disposal after it requires a documented
 *  authorisation — the system enforces the date, humans authorise the act. */
public final class RetentionRules {
  private RetentionRules() {}
  public static int retentionYears(String category) {
    return switch (category == null ? "" : category) {
      case "death_certificate", "birth_record" -> 100; // permanent-class records
      case "operative_report", "discharge_summary", "consent_form" -> 25;
      case "lab_report", "radiology_report", "prescription_copy" -> 15;
      default -> 10;
    };
  }
  public static LocalDate disposalEligibleFrom(LocalDate documentDate, String category) {
    return documentDate.plusYears(retentionYears(category));
  }
  public static boolean disposalAllowed(LocalDate documentDate, String category, LocalDate onDate, boolean authorised) {
    return authorised && !onDate.isBefore(disposalEligibleFrom(documentDate, category));
  }
}""")
w(PKG/"governance/MedicalDocument.java", """package eg.gov.health.platform.governance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "medical_documents")
public class MedicalDocument {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String category;
  @Column(nullable=false) private String title;
  @Column(name="document_date", nullable=false) private LocalDate documentDate;
  @Column(name="storage_uri") private String storageUri;
  @Column(nullable=false) private String status = "active"; // active, archived, disposed
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected MedicalDocument() {}
  public MedicalDocument(UUID facilityId, String governorate, UUID patientId, String category, String title, LocalDate documentDate, String storageUri) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.category=category;
    this.title=title; this.documentDate=documentDate; this.storageUri=storageUri;
  }
  public UUID getId(){return id;} public LocalDate getDocumentDate(){return documentDate;} public String getCategory(){return category;}
  public String getStatus(){return status;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"governance/MedicalDocumentRepository.java", """package eg.gov.health.platform.governance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, UUID> {
  List<MedicalDocument> findByPatientIdOrderByDocumentDateDesc(UUID patientId);
}""")
w(PKG/"governance/DocumentsController.java", """package eg.gov.health.platform.governance;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/documents")
public class DocumentsController {
  private final MedicalDocumentRepository repo; private final AuditService audit;
  public DocumentsController(MedicalDocumentRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record DocRequest(UUID patientId, String category, String title, LocalDate documentDate, String storageUri) {}
  @PostMapping
  Map<String,Object> add(@RequestBody DocRequest r) {
    var p = TenantContext.get();
    if (r.patientId()==null||r.category()==null||r.title()==null||r.documentDate()==null) throw BizException.badRequest("missing_field","بيانات الوثيقة ناقصة");
    var d = repo.save(new MedicalDocument(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.category(), r.title(), r.documentDate(), r.storageUri()));
    audit.record("document.added","medical_document",d.getId().toString(),"{}");
    return Map.of("ok",true,"document",d,"retention_years",RetentionRules.retentionYears(r.category()),
      "disposal_eligible_from",RetentionRules.disposalEligibleFrom(r.documentDate(), r.category()));
  }
  @PostMapping("/{id}/dispose")
  Map<String,Object> dispose(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var d = repo.findById(id).orElseThrow(() -> BizException.notFound("document_not_found","الوثيقة غير موجودة"));
    boolean authorised = Boolean.parseBoolean(body.getOrDefault("authorised","false"));
    if (!RetentionRules.disposalAllowed(d.getDocumentDate(), d.getCategory(), LocalDate.now(), authorised))
      throw BizException.conflict("retention_not_met","الإتلاف ممنوع قبل انتهاء مدة الاحتفاظ أو بلا تصريح موثق");
    d.setStatus("disposed"); repo.save(d);
    audit.record("document.disposed","medical_document",id.toString(),"{}");
    return Map.of("ok",true,"document",d);
  }
  @GetMapping
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = repo.findByPatientIdOrderByDocumentDateDesc(patientId);
    return Map.of("ok",true,"documents",rows,"count",rows.size());
  }
}""")

# ---------------- procurement + asset maintenance
w(PKG/"operations/PurchaseOrder.java", """package eg.gov.health.platform.operations;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "purchase_orders")
public class PurchaseOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(nullable=false) private String supplier;
  @Column(name="item_code", nullable=false) private String itemCode;
  @Column(nullable=false) private int quantity;
  @Column(name="unit_price", nullable=false) private BigDecimal unitPrice;
  @Column(nullable=false) private String status = "draft"; // draft, approved, ordered, partially_received, received, cancelled
  @Column(name="approved_by") private String approvedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PurchaseOrder() {}
  public PurchaseOrder(UUID facilityId, String governorate, String supplier, String itemCode, int quantity, BigDecimal unitPrice) {
    this.facilityId=facilityId; this.governorate=governorate; this.supplier=supplier; this.itemCode=itemCode;
    this.quantity=quantity; this.unitPrice=unitPrice;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getItemCode(){return itemCode;}
  public int getQuantity(){return quantity;} public UUID getFacilityId(){return facilityId;}
  public BigDecimal total(){ return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
  public void approve(String by){this.approvedBy=by; this.status="approved"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"operations/PurchaseOrderRepository.java", """package eg.gov.health.platform.operations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
  List<PurchaseOrder> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
}""")
w(PKG/"operations/ProcurementController.java", """package eg.gov.health.platform.operations;
import eg.gov.health.platform.common.*;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.util.*;
@RestController @RequestMapping("/api/procurement")
public class ProcurementController {
  private final PurchaseOrderRepository repo; private final StockItemRepository stock; private final AuditService audit;
  public ProcurementController(PurchaseOrderRepository repo, StockItemRepository stock, AuditService audit) { this.repo=repo; this.stock=stock; this.audit=audit; }
  public record PoRequest(String supplier, String itemCode, Integer quantity, BigDecimal unitPrice) {}
  @PostMapping("/orders")
  Map<String,Object> create(@RequestBody PoRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.supplier()==null||r.itemCode()==null||r.quantity()==null||r.quantity()<=0||r.unitPrice()==null)
      throw BizException.badRequest("invalid_po","بيانات أمر الشراء غير صحيحة");
    var po = repo.save(new PurchaseOrder(UUID.fromString(p.facilityId()), p.governorate(), r.supplier(), r.itemCode(), r.quantity(), r.unitPrice()));
    return Map.of("ok",true,"order",po,"total",po.total());
  }
  @PostMapping("/orders/{id}/approve")
  Map<String,Object> approve(@PathVariable UUID id) {
    var p = TenantContext.get();
    var po = repo.findById(id).orElseThrow(() -> BizException.notFound("po_not_found","الأمر غير موجود"));
    if (!"draft".equals(po.getStatus())) throw BizException.conflict("not_draft","الاعتماد من المسودة فقط");
    po.approve(p==null?null:p.userId()); repo.save(po);
    audit.record("procurement.approved","purchase_order",id.toString(),"{}");
    return Map.of("ok",true,"order",po);
  }
  @PostMapping("/orders/{id}/receive") @Transactional
  Map<String,Object> receive(@PathVariable UUID id, @RequestBody Map<String,Integer> body) {
    var po = repo.findById(id).orElseThrow(() -> BizException.notFound("po_not_found","الأمر غير موجود"));
    if (!List.of("approved","ordered","partially_received").contains(po.getStatus()))
      throw BizException.conflict("not_receivable","الاستلام بعد الاعتماد فقط — لا استلام لأمر غير معتمد");
    int received = body.getOrDefault("quantity", 0);
    if (received <= 0 || received > po.getQuantity()) throw BizException.badRequest("invalid_quantity","كمية استلام غير صحيحة");
    var item = stock.findByFacilityIdAndItemCode(po.getFacilityId(), po.getItemCode())
      .orElseThrow(() -> BizException.notFound("item_not_found","الصنف غير معرف في المخزون"));
    if (!item.applyMovement(received)) throw BizException.conflict("stock_error","تعذر ترحيل الاستلام للمخزون");
    stock.save(item);
    po.setStatus(received < po.getQuantity() ? "partially_received" : "received"); repo.save(po);
    audit.record("procurement.received","purchase_order",id.toString(),"{}");
    return Map.of("ok",true,"order",po,"stock_quantity",item.getQuantity());
  }
  @GetMapping("/orders")
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"orders",rows,"count",rows.size());
  }
}""")
w(PKG/"operations/Asset.java", """package eg.gov.health.platform.operations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "assets")
public class Asset {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="asset_code", nullable=false, unique=true) private String assetCode;
  @Column(nullable=false) private String name;
  @Column(name="next_maintenance") private LocalDate nextMaintenance;
  @Column(nullable=false) private String status = "operational"; // operational, under_maintenance, out_of_service, retired
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Asset() {}
  public Asset(UUID facilityId, String governorate, String assetCode, String name, LocalDate nextMaintenance) {
    this.facilityId=facilityId; this.governorate=governorate; this.assetCode=assetCode; this.name=name; this.nextMaintenance=nextMaintenance;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public LocalDate getNextMaintenance(){return nextMaintenance;}
  public void completeMaintenance(LocalDate next){this.nextMaintenance=next; this.status="operational"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"operations/AssetRepository.java", """package eg.gov.health.platform.operations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AssetRepository extends JpaRepository<Asset, UUID> {
  List<Asset> findByFacilityIdOrderByNextMaintenanceAsc(UUID facilityId);
  List<Asset> findByFacilityIdAndNextMaintenanceLessThanEqual(UUID facilityId, java.time.LocalDate date);
}""")
w(PKG/"operations/AssetsController.java", """package eg.gov.health.platform.operations;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/assets")
public class AssetsController {
  private final AssetRepository repo; private final AuditService audit;
  public AssetsController(AssetRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record AssetRequest(String assetCode, String name, LocalDate nextMaintenance) {}
  @PostMapping
  Map<String,Object> add(@RequestBody AssetRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.assetCode()==null||r.name()==null) throw BizException.badRequest("missing_field","assetCode و name مطلوبان");
    var a = repo.save(new Asset(UUID.fromString(p.facilityId()), p.governorate(), r.assetCode(), r.name(), r.nextMaintenance()));
    return Map.of("ok",true,"asset",a);
  }
  @PostMapping("/{id}/maintenance-complete")
  Map<String,Object> maintenance(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var a = repo.findById(id).orElseThrow(() -> BizException.notFound("asset_not_found","الأصل غير موجود"));
    LocalDate next = body.get("next_maintenance")==null ? LocalDate.now().plusMonths(6) : LocalDate.parse(body.get("next_maintenance"));
    a.completeMaintenance(next); repo.save(a);
    audit.record("asset.maintenance_completed","asset",id.toString(),"{}");
    return Map.of("ok",true,"asset",a);
  }
  @GetMapping("/due")
  Map<String,Object> due() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdAndNextMaintenanceLessThanEqual(UUID.fromString(p.facilityId()), LocalDate.now().plusDays(30));
    return Map.of("ok",true,"due_within_30_days",rows,"count",rows.size());
  }
  @GetMapping
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByNextMaintenanceAsc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"assets",rows,"count",rows.size());
  }
}""")
print("batch6 java done")

# ---------------- Flyway V7 + tests
rls_fac = """ALTER TABLE {t} ENABLE ROW LEVEL SECURITY;
ALTER TABLE {t} FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS {t}_facility_isolation ON {t};
CREATE POLICY {t}_facility_isolation ON {t} FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));"""
v7 = """-- V7 — Batch 6 (Catalog v6): dialysis 242, oncology, telemedicine,
-- death registry, documents/retention, procurement, assets.
CREATE TABLE IF NOT EXISTS dialysis_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  machine_code TEXT NOT NULL, starts_at TIMESTAMPTZ NOT NULL, ends_at TIMESTAMPTZ NOT NULL,
  dry_weight_kg DOUBLE PRECISION, ultrafiltration_ml INT,
  status TEXT NOT NULL DEFAULT 'scheduled' CHECK (status IN ('scheduled','in_session','completed','missed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_dialysis_window CHECK (ends_at > starts_at));
CREATE INDEX IF NOT EXISTS idx_dialysis_machine ON dialysis_sessions (facility_id, machine_code, starts_at);
CREATE TABLE IF NOT EXISTS chemo_cycles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  protocol TEXT, drug_code TEXT NOT NULL, cycle_number INT NOT NULL,
  bsa DOUBLE PRECISION NOT NULL, dose_mg NUMERIC NOT NULL, scheduled_date DATE,
  status TEXT NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','verified','administered','held','cancelled')),
  verified_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_chemo_patient ON chemo_cycles (patient_id, cycle_number);
CREATE TABLE IF NOT EXISTS teleconsultations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  consultant_name TEXT NOT NULL, specialty TEXT, scheduled_at TIMESTAMPTZ NOT NULL,
  status TEXT NOT NULL DEFAULT 'scheduled' CHECK (status IN ('scheduled','in_session','completed','no_show','cancelled','escalated_to_in_person')),
  session_notes TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS death_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL UNIQUE REFERENCES patients(id),
  died_at TIMESTAMPTZ NOT NULL, cause_icd10 TEXT NOT NULL, cause_description TEXT, certified_by TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'certified' CHECK (status IN ('certified','under_review','corrected')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS medical_documents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  category TEXT NOT NULL, title TEXT NOT NULL, document_date DATE NOT NULL, storage_uri TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','archived','disposed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_documents_patient ON medical_documents (patient_id, document_date);
CREATE TABLE IF NOT EXISTS purchase_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  supplier TEXT NOT NULL, item_code TEXT NOT NULL, quantity INT NOT NULL, unit_price NUMERIC NOT NULL,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','approved','ordered','partially_received','received','cancelled')),
  approved_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_po_qty CHECK (quantity > 0));
CREATE TABLE IF NOT EXISTS assets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  asset_code TEXT NOT NULL UNIQUE, name TEXT NOT NULL, next_maintenance DATE,
  status TEXT NOT NULL DEFAULT 'operational' CHECK (status IN ('operational','under_maintenance','out_of_service','retired')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
""" + "\n".join(rls_fac.format(t=t) for t in ["dialysis_sessions","chemo_cycles","purchase_orders","assets"]) + """
GRANT SELECT, INSERT, UPDATE, DELETE ON dialysis_sessions, chemo_cycles, teleconsultations, death_records, medical_documents, purchase_orders, assets TO app_user;
"""
w(RES/"db/migration/V7__programs_telemedicine_death_documents_procurement_assets.sql", v7)

w(TEST/"programs/ChemoDosingTest.java", """package eg.gov.health.platform.programs;
import org.junit.jupiter.api.Test; import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class ChemoDosingTest {
  @Test void bsaMostellerKnownValue() {
    // 170cm, 70kg -> sqrt(170*70/3600) = sqrt(3.3056) = 1.8181...
    assertEquals(1.818, ChemoDosing.bsaMosteller(170, 70), 0.001);
  }
  @Test void fullDoseAtNormalRenalFunction() {
    var dose = ChemoDosing.adjustedDoseMg(1.818, new BigDecimal("75"), 90);
    assertEquals(new BigDecimal("136.4"), dose);
  }
  @Test void renalBands() {
    assertEquals(new BigDecimal("75"), ChemoDosing.renalAdjustmentPct(50));
    assertEquals(new BigDecimal("50"), ChemoDosing.renalAdjustmentPct(30));
    assertEquals(BigDecimal.ZERO, ChemoDosing.renalAdjustmentPct(15));
  }
  @Test void invalidAnthropometricsRejected() { assertThrows(IllegalArgumentException.class, () -> ChemoDosing.bsaMosteller(0, 70)); }
}""")
w(TEST/"governance/RetentionRulesTest.java", """package eg.gov.health.platform.governance;
import org.junit.jupiter.api.Test; import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class RetentionRulesTest {
  @Test void retentionPeriods() {
    assertEquals(25, RetentionRules.retentionYears("discharge_summary"));
    assertEquals(15, RetentionRules.retentionYears("lab_report"));
    assertEquals(100, RetentionRules.retentionYears("death_certificate"));
    assertEquals(10, RetentionRules.retentionYears("other"));
  }
  @Test void disposalGating() {
    var docDate = LocalDate.of(2020, 1, 1);
    assertFalse(RetentionRules.disposalAllowed(docDate, "lab_report", LocalDate.of(2030, 1, 1), true));
    assertTrue(RetentionRules.disposalAllowed(docDate, "lab_report", LocalDate.of(2036, 1, 1), true));
    assertFalse(RetentionRules.disposalAllowed(docDate, "lab_report", LocalDate.of(2036, 1, 1), false));
  }
}""")
print("batch6 migration+tests done")
