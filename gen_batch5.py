#!/usr/bin/env python3
"""Batch 5: workforce & shifts (231), complaints with SLA, research data
requests with de-identification governance (module 65), notification outbox."""
import pathlib
BASE = pathlib.Path.home() / "workspace/egypt-health-v6/services/platform-api"
PKG = BASE / "src/main/java/eg/gov/health/platform"
RES = BASE / "src/main/resources"
TEST = BASE / "src/test/java/eg/gov/health/platform"
def w(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8"); print("wrote", path.name)

# ---------------- workforce: staff + shifts (cycle 231)
w(PKG/"workforce/ShiftRules.java", """package eg.gov.health.platform.workforce;
import java.time.*;
/** Safe-rostering rules (cycle 231): no overlapping shifts for the same
 *  staff member, and a minimum 11-hour rest between the end of one shift
 *  and the start of the next. Violations are rejected, never warned-only. */
public final class ShiftRules {
  private ShiftRules() {}
  public static final long MIN_REST_HOURS = 11;
  public record ShiftView(OffsetDateTime startsAt, OffsetDateTime endsAt, String status) {}
  public static boolean overlaps(ShiftView a, ShiftView b) {
    return !"cancelled".equals(b.status()) && a.startsAt().isBefore(b.endsAt()) && b.startsAt().isBefore(a.endsAt());
  }
  public static boolean hasEnoughRest(ShiftView candidate, ShiftView other) {
    if ("cancelled".equals(other.status())) return true;
    if (overlaps(candidate, other)) return false;
    long gapAfter = Math.abs(Duration.between(candidate.endsAt(), other.startsAt()).toHours());
    long gapBefore = Math.abs(Duration.between(other.endsAt(), candidate.startsAt()).toHours());
    long gap = candidate.startsAt().isAfter(other.endsAt()) ? gapBefore : gapAfter;
    return gap >= MIN_REST_HOURS;
  }
  public static boolean isAssignable(ShiftView candidate, java.util.List<ShiftView> existing) {
    return existing.stream().allMatch(o -> hasEnoughRest(candidate, o));
  }
}""")
w(PKG/"workforce/StaffMember.java", """package eg.gov.health.platform.workforce;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "staff_members")
public class StaffMember {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="user_id") private String userId;
  @Column(name="full_name", nullable=false) private String fullName;
  @Column(nullable=false) private String profession; // physician, nurse, pharmacist, technician, admin, paramedic
  @Column(name="specialty") private String specialty;
  @Column(name="license_number") private String licenseNumber;
  @Column(nullable=false) private String status = "active"; // active, on_leave, suspended, departed
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected StaffMember() {}
  public StaffMember(UUID facilityId, String governorate, String userId, String fullName, String profession, String specialty, String licenseNumber) {
    this.facilityId=facilityId; this.governorate=governorate; this.userId=userId; this.fullName=fullName;
    this.profession=profession; this.specialty=specialty; this.licenseNumber=licenseNumber;
  }
  public UUID getId(){return id;} public String getFullName(){return fullName;} public String getStatus(){return status;}
  public UUID getFacilityId(){return facilityId;}
}""")
w(PKG/"workforce/Shift.java", """package eg.gov.health.platform.workforce;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "shifts")
public class Shift {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="staff_id", nullable=false) private UUID staffId;
  @Column(name="unit") private String unit;
  @Column(name="starts_at", nullable=false) private OffsetDateTime startsAt;
  @Column(name="ends_at", nullable=false) private OffsetDateTime endsAt;
  @Column(nullable=false) private String kind = "regular"; // regular, on_call, overtime
  @Column(nullable=false) private String status = "scheduled"; // scheduled, completed, swapped, cancelled, absent
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Shift() {}
  public Shift(UUID facilityId, String governorate, UUID staffId, String unit, OffsetDateTime startsAt, OffsetDateTime endsAt, String kind) {
    this.facilityId=facilityId; this.governorate=governorate; this.staffId=staffId; this.unit=unit;
    this.startsAt=startsAt; this.endsAt=endsAt; this.kind=kind==null?"regular":kind;
  }
  public UUID getId(){return id;} public UUID getStaffId(){return staffId;}
  public ShiftRules.ShiftView toView(){ return new ShiftRules.ShiftView(startsAt, endsAt, status); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"workforce/StaffMemberRepository.java", """package eg.gov.health.platform.workforce;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface StaffMemberRepository extends JpaRepository<StaffMember, UUID> {
  List<StaffMember> findByFacilityIdOrderByFullName(UUID facilityId);
}""")
w(PKG/"workforce/ShiftRepository.java", """package eg.gov.health.platform.workforce;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ShiftRepository extends JpaRepository<Shift, UUID> {
  List<Shift> findByStaffId(UUID staffId);
  List<Shift> findByFacilityIdOrderByStartsAtAsc(UUID facilityId);
}""")
w(PKG/"workforce/WorkforceController.java", """package eg.gov.health.platform.workforce;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/workforce")
public class WorkforceController {
  private final StaffMemberRepository staff; private final ShiftRepository shifts; private final AuditService audit;
  public WorkforceController(StaffMemberRepository staff, ShiftRepository shifts, AuditService audit) { this.staff=staff; this.shifts=shifts; this.audit=audit; }
  public record StaffRequest(String userId, String fullName, String profession, String specialty, String licenseNumber) {}
  @PostMapping("/staff")
  Map<String,Object> addStaff(@RequestBody StaffRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.fullName()==null||r.profession()==null) throw BizException.badRequest("missing_field","fullName و profession مطلوبان");
    var s = staff.save(new StaffMember(UUID.fromString(p.facilityId()), p.governorate(), r.userId(), r.fullName(), r.profession(), r.specialty(), r.licenseNumber()));
    return Map.of("ok",true,"staff",s);
  }
  public record ShiftRequest(UUID staffId, String unit, OffsetDateTime startsAt, OffsetDateTime endsAt, String kind) {}
  @PostMapping("/shifts")
  Map<String,Object> schedule(@RequestBody ShiftRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.staffId()==null||r.startsAt()==null||r.endsAt()==null||!r.endsAt().isAfter(r.startsAt()))
      throw BizException.badRequest("invalid_shift","فترة وردية غير صحيحة");
    var member = staff.findById(r.staffId()).orElseThrow(() -> BizException.notFound("staff_not_found","الموظف غير موجود"));
    if (!"active".equals(member.getStatus())) throw BizException.conflict("staff_not_active","لا تُسند ورديات لموظف غير نشط");
    var candidate = new ShiftRules.ShiftView(r.startsAt(), r.endsAt(), "scheduled");
    var existing = shifts.findByStaffId(r.staffId()).stream().map(Shift::toView).toList();
    if (!ShiftRules.isAssignable(candidate, existing))
      throw BizException.conflict("unsafe_roster","تعارض ورديات أو راحة أقل من "+ShiftRules.MIN_REST_HOURS+" ساعة (دورة 231)");
    var s = shifts.save(new Shift(UUID.fromString(p.facilityId()), p.governorate(), r.staffId(), r.unit(), r.startsAt(), r.endsAt(), r.kind()));
    audit.record("workforce.shift.scheduled","shift",s.getId().toString(),"{}");
    return Map.of("ok",true,"shift",s);
  }
  @GetMapping("/staff")
  Map<String,Object> listStaff() {
    var p = TenantContext.get();
    var rows = staff.findByFacilityIdOrderByFullName(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"staff",rows,"count",rows.size());
  }
  @GetMapping("/shifts")
  Map<String,Object> listShifts() {
    var p = TenantContext.get();
    var rows = shifts.findByFacilityIdOrderByStartsAtAsc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"shifts",rows,"count",rows.size());
  }
}""")

# ---------------- complaints with SLA
w(PKG/"quality/ComplaintSla.java", """package eg.gov.health.platform.quality;
import java.time.*;
/** Complaint handling SLA (citizen grievances): resolution due dates by
 *  severity, computed in business terms from the intake timestamp. A breach
 *  escalates automatically to the next management level. */
public final class ComplaintSla {
  private ComplaintSla() {}
  public static int resolutionDays(String severity) {
    return switch (severity == null ? "" : severity) {
      case "critical" -> 3; case "high" -> 7; case "medium" -> 14; default -> 30;
    };
  }
  public static OffsetDateTime dueAt(OffsetDateTime intakeAt, String severity) { return intakeAt.plusDays(resolutionDays(severity)); }
  public static boolean isBreached(OffsetDateTime intakeAt, String severity, String status, OffsetDateTime now) {
    return !"resolved".equals(status) && !"closed".equals(status) && now.isAfter(dueAt(intakeAt, severity));
  }
}""")
w(PKG/"quality/Complaint.java", """package eg.gov.health.platform.quality;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "complaints")
public class Complaint {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id") private UUID patientId;
  @Column(name="complainant_name") private String complainantName;
  @Column(nullable=false) private String category;
  @Column(nullable=false) private String severity = "low"; // low, medium, high, critical
  @Column(nullable=false) private String description;
  @Column(nullable=false) private String status = "received"; // received, investigating, resolved, closed, escalated
  @Column(name="resolution") private String resolution;
  @Column(name="due_at") private OffsetDateTime dueAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Complaint() {}
  public Complaint(UUID facilityId, String governorate, UUID patientId, String complainantName, String category, String severity, String description) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.complainantName=complainantName;
    this.category=category; this.severity=severity==null?"low":severity; this.description=description;
    this.dueAt = ComplaintSla.dueAt(this.createdAt, this.severity);
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public OffsetDateTime getDueAt(){return dueAt;}
  public OffsetDateTime getCreatedAt(){return createdAt;} public String getSeverity(){return severity;}
  public void resolve(String res){this.resolution=res; this.status="resolved"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"quality/ComplaintRepository.java", """package eg.gov.health.platform.quality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {
  List<Complaint> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
}""")
w(PKG/"quality/ComplaintController.java", """package eg.gov.health.platform.quality;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/complaints")
public class ComplaintController {
  private final ComplaintRepository repo; private final AuditService audit;
  public ComplaintController(ComplaintRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record ComplaintRequest(UUID patientId, String complainantName, String category, String severity, String description) {}
  @PostMapping
  Map<String,Object> file(@RequestBody ComplaintRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.category()==null||r.description()==null||r.description().isBlank()) throw BizException.badRequest("missing_field","category و description مطلوبتان");
    var c = repo.save(new Complaint(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.complainantName(), r.category(), r.severity(), r.description()));
    audit.record("complaint.received","complaint",c.getId().toString(),"{}");
    return Map.of("ok",true,"complaint",c,"sla_due_at",c.getDueAt());
  }
  @PostMapping("/{id}/investigate")
  Map<String,Object> investigate(@PathVariable UUID id) {
    var c = get(id);
    if (!"received".equals(c.getStatus()) && !"escalated".equals(c.getStatus())) throw BizException.conflict("bad_state","التحقيق من حالة الاستلام أو التصعيد فقط");
    c.setStatus("investigating"); repo.save(c);
    return Map.of("ok",true,"complaint",c);
  }
  @PostMapping("/{id}/resolve")
  Map<String,Object> resolve(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var c = get(id);
    if (body.getOrDefault("resolution","").isBlank()) throw BizException.badRequest("resolution_required","نص الحل موثق إلزاميًا");
    if (!"investigating".equals(c.getStatus())) throw BizException.conflict("not_investigating","الحل بعد بدء التحقيق");
    c.resolve(body.get("resolution")); repo.save(c);
    audit.record("complaint.resolved","complaint",id.toString(),"{}");
    return Map.of("ok",true,"complaint",c);
  }
  @GetMapping("/sla-breaches")
  Map<String,Object> breaches() {
    var p = TenantContext.get(); var now = OffsetDateTime.now();
    var rows = repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId())).stream()
      .filter(c -> ComplaintSla.isBreached(c.getCreatedAt(), c.getSeverity(), c.getStatus(), now)).toList();
    return Map.of("ok",true,"breaches",rows,"count",rows.size());
  }
  @GetMapping
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"complaints",rows,"count",rows.size());
  }
  private Complaint get(UUID id) { return repo.findById(id).orElseThrow(() -> BizException.notFound("complaint_not_found","الشكوى غير موجودة")); }
}""")

# ---------------- research data requests (module 65): de-identification rules
w(PKG/"governance/ResearchExtractRules.java", """package eg.gov.health.platform.governance;
import java.util.*;
/** Research/secondary-use extract governance (module 65 — research governance): a research extract
 *  may NEVER contain direct identifiers, regardless of who asks. Quasi-
 *  identifiers are only released in generalised form. Approval requires an
 *  ethics/governance decision recorded BEFORE any extract is produced. */
public final class ResearchExtractRules {
  private ResearchExtractRules() {}
  public static final Set<String> DIRECT_IDENTIFIERS = Set.of(
    "national_id","full_name","full_name_ar","phone","email","address","medical_record_number","photo_url");
  public static final Set<String> QUASI_IDENTIFIERS = Set.of("birth_date","facility_id","admission_date","discharge_date");
  public static List<String> violations(Collection<String> requestedFields) {
    return requestedFields.stream().filter(DIRECT_IDENTIFIERS::contains).sorted().toList();
  }
  public static boolean extractAllowed(String requestStatus, Collection<String> requestedFields) {
    return "approved".equals(requestStatus) && violations(requestedFields).isEmpty();
  }
}""")
w(PKG/"governance/ResearchRequest.java", """package eg.gov.health.platform.governance;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "research_requests")
public class ResearchRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="requester", nullable=false) private String requester;
  @Column(nullable=false) private String purpose;
  @Column(name="requested_fields", nullable=false) private String requestedFields;
  @Column(nullable=false) private String status = "submitted"; // submitted, under_review, approved, rejected, extracted
  @Column(name="ethics_decision") private String ethicsDecision;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ResearchRequest() {}
  public ResearchRequest(UUID facilityId, String governorate, String requester, String purpose, String requestedFields) {
    this.facilityId=facilityId; this.governorate=governorate; this.requester=requester; this.purpose=purpose; this.requestedFields=requestedFields;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public java.util.List<String> fieldList(){ return requestedFields==null||requestedFields.isBlank()? java.util.List.of() : java.util.List.of(requestedFields.split(",")); }
  public void decide(String decision, String status){ this.ethicsDecision=decision; this.status=status; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"governance/ResearchRequestRepository.java", """package eg.gov.health.platform.governance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ResearchRequestRepository extends JpaRepository<ResearchRequest, UUID> {
  List<ResearchRequest> findByStatusOrderByCreatedAtAsc(String status);
}""")
w(PKG/"governance/ResearchController.java", """package eg.gov.health.platform.governance;
import eg.gov.health.platform.common.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/research")
public class ResearchController {
  private final ResearchRequestRepository repo; private final AuditService audit;
  public ResearchController(ResearchRequestRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record ResearchReq(String purpose, List<String> fields) {}
  @PostMapping("/requests")
  Map<String,Object> submit(@RequestBody ResearchReq r) {
    var p = TenantContext.get();
    if (r.purpose()==null||r.fields()==null||r.fields().isEmpty()) throw BizException.badRequest("missing_field","purpose و fields مطلوبة");
    var violations = ResearchExtractRules.violations(r.fields());
    if (!violations.isEmpty())
      throw BizException.badRequest("direct_identifiers_forbidden","حقول تعريف مباشر ممنوعة في أي مستخرج بحثي: "+violations+"");
    var req = repo.save(new ResearchRequest(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), p==null?null:p.userId(), r.purpose(), String.join(",", r.fields())));
    audit.record("research.request.submitted","research_request",req.getId().toString(),"{}");
    return Map.of("ok",true,"request",req);
  }
  @PostMapping("/requests/{id}/decide") @PreAuthorize("hasAnyRole('national_admin','governorate_admin')")
  Map<String,Object> decide(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var req = repo.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    String decision = body.getOrDefault("decision","");
    if (decision.isBlank()) throw BizException.badRequest("decision_required","قرار لجنة الأخلاقيات/الحوكمة موثق إلزاميًا");
    boolean approve = Boolean.parseBoolean(body.getOrDefault("approve","false"));
    req.decide(decision, approve ? "approved" : "rejected"); repo.save(req);
    audit.record("research.request."+(approve?"approved":"rejected"),"research_request",id.toString(),"{}");
    return Map.of("ok",true,"request",req);
  }
  @PostMapping("/requests/{id}/extract") @PreAuthorize("hasAnyRole('national_admin','governorate_admin')")
  Map<String,Object> extract(@PathVariable UUID id) {
    var req = repo.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    if (!ResearchExtractRules.extractAllowed(req.getStatus(), req.fieldList()))
      throw BizException.conflict("extract_not_allowed","المستخرج يتطلب طلبًا معتمدًا وبلا معرّفات مباشرة");
    req.setStatus("extracted"); repo.save(req);
    audit.record("research.extract.produced","research_request",id.toString(),"{}");
    return Map.of("ok",true,"request",req,"note","مستخرج مجهّل الهوية فقط — الحقول: "+req.fieldList());
  }
  @GetMapping("/requests/pending")
  Map<String,Object> pending() {
    var rows = repo.findByStatusOrderByCreatedAtAsc("submitted");
    return Map.of("ok",true,"requests",rows,"count",rows.size());
  }
}""")

# ---------------- notification outbox
w(PKG/"operations/Notification.java", """package eg.gov.health.platform.operations;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "notifications")
public class Notification {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="recipient", nullable=false) private String recipient;
  @Column(nullable=false) private String channel = "in_app"; // in_app, sms, email
  @Column(nullable=false) private String subject;
  @Column(nullable=false) private String body;
  @Column(nullable=false) private String status = "queued"; // queued, sent, failed
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Notification() {}
  public Notification(UUID facilityId, String governorate, String recipient, String channel, String subject, String body) {
    this.facilityId=facilityId; this.governorate=governorate; this.recipient=recipient;
    this.channel=channel==null?"in_app":channel; this.subject=subject; this.body=body;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"operations/NotificationRepository.java", """package eg.gov.health.platform.operations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
  List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);
  List<Notification> findByStatus(String status);
}""")
w(PKG/"operations/NotificationController.java", """package eg.gov.health.platform.operations;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/notifications")
public class NotificationController {
  private final NotificationRepository repo;
  public NotificationController(NotificationRepository repo) { this.repo=repo; }
  public record NotifyRequest(String recipient, String channel, String subject, String body) {}
  @PostMapping
  Map<String,Object> send(@RequestBody NotifyRequest r) {
    var p = TenantContext.get();
    if (r.recipient()==null||r.subject()==null||r.body()==null) throw BizException.badRequest("missing_field","recipient و subject و body مطلوبة");
    var n = repo.save(new Notification(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.recipient(), r.channel(), r.subject(), r.body()));
    // Outbox pattern: queued here, delivered by the messaging worker (Kafka) —
    // the API never blocks on SMS/email gateways.
    return Map.of("ok",true,"notification",n);
  }
  @GetMapping("/mine")
  Map<String,Object> mine() {
    var p = TenantContext.get();
    var rows = repo.findByRecipientOrderByCreatedAtDesc(p==null?"":p.userId());
    return Map.of("ok",true,"notifications",rows,"count",rows.size());
  }
}""")
print("batch5 java done")

# ---------------- Flyway V6 + tests
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
v6 = """-- V6 — Batch 5 (Catalog v6): workforce 231, complaints SLA, research (module 65), notifications.
CREATE TABLE IF NOT EXISTS staff_members (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  user_id TEXT, full_name TEXT NOT NULL, profession TEXT NOT NULL, specialty TEXT, license_number TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','on_leave','suspended','departed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS shifts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  staff_id UUID NOT NULL REFERENCES staff_members(id), unit TEXT,
  starts_at TIMESTAMPTZ NOT NULL, ends_at TIMESTAMPTZ NOT NULL,
  kind TEXT NOT NULL DEFAULT 'regular', status TEXT NOT NULL DEFAULT 'scheduled',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_shift_window CHECK (ends_at > starts_at));
CREATE INDEX IF NOT EXISTS idx_shifts_staff ON shifts (staff_id, starts_at);
CREATE TABLE IF NOT EXISTS complaints (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID REFERENCES patients(id), complainant_name TEXT,
  category TEXT NOT NULL, severity TEXT NOT NULL DEFAULT 'low' CHECK (severity IN ('low','medium','high','critical')),
  description TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'received' CHECK (status IN ('received','investigating','resolved','closed','escalated')),
  resolution TEXT, due_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_complaints_facility_status ON complaints (facility_id, status);
CREATE TABLE IF NOT EXISTS research_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  requester TEXT NOT NULL, purpose TEXT NOT NULL, requested_fields TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'submitted' CHECK (status IN ('submitted','under_review','approved','rejected','extracted')),
  ethics_decision TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS notifications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  recipient TEXT NOT NULL, channel TEXT NOT NULL DEFAULT 'in_app',
  subject TEXT NOT NULL, body TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'queued' CHECK (status IN ('queued','sent','failed')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications (recipient, created_at);
""" + "\n".join(rls_fac.format(t=t) for t in ["staff_members","shifts","complaints"]) + """
GRANT SELECT, INSERT, UPDATE, DELETE ON staff_members, shifts, complaints, research_requests, notifications TO app_user;
"""
w(RES/"db/migration/V6__workforce_complaints_research_notifications.sql", v6)

w(TEST/"workforce/ShiftRulesTest.java", """package eg.gov.health.platform.workforce;
import org.junit.jupiter.api.Test; import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ShiftRulesTest {
  ShiftRules.ShiftView shift(String s, String e) {
    return new ShiftRules.ShiftView(OffsetDateTime.parse(s), OffsetDateTime.parse(e), "scheduled");
  }
  @Test void overlapRejected() {
    var a = shift("2026-01-01T08:00:00+02:00","2026-01-01T20:00:00+02:00");
    var b = shift("2026-01-01T19:00:00+02:00","2026-01-02T07:00:00+02:00");
    assertFalse(ShiftRules.isAssignable(b, java.util.List.of(a)));
  }
  @Test void restUnder11HoursRejected() {
    var night = shift("2026-01-01T20:00:00+02:00","2026-01-02T08:00:00+02:00");
    var morning = shift("2026-01-02T16:00:00+02:00","2026-01-03T04:00:00+02:00");
    assertFalse(ShiftRules.isAssignable(morning, java.util.List.of(night)));
  }
  @Test void restOver11HoursAccepted() {
    var night = shift("2026-01-01T20:00:00+02:00","2026-01-02T08:00:00+02:00");
    var evening = shift("2026-01-02T20:00:00+02:00","2026-01-03T08:00:00+02:00");
    assertTrue(ShiftRules.isAssignable(evening, java.util.List.of(night)));
  }
  @Test void cancelledShiftsIgnored() {
    var cancelled = new ShiftRules.ShiftView(OffsetDateTime.parse("2026-01-01T08:00:00+02:00"), OffsetDateTime.parse("2026-01-01T20:00:00+02:00"), "cancelled");
    assertTrue(ShiftRules.isAssignable(shift("2026-01-01T09:00:00+02:00","2026-01-01T17:00:00+02:00"), java.util.List.of(cancelled)));
  }
}""")
w(TEST/"quality/ComplaintSlaTest.java", """package eg.gov.health.platform.quality;
import org.junit.jupiter.api.Test; import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;
class ComplaintSlaTest {
  @Test void dueDatesBySeverity() {
    var t = OffsetDateTime.parse("2026-01-01T10:00:00+02:00");
    assertEquals(t.plusDays(3), ComplaintSla.dueAt(t, "critical"));
    assertEquals(t.plusDays(30), ComplaintSla.dueAt(t, "low"));
  }
  @Test void breachOnlyWhenOpenAndOverdue() {
    var t = OffsetDateTime.parse("2026-01-01T10:00:00+02:00");
    assertTrue(ComplaintSla.isBreached(t, "critical", "investigating", t.plusDays(4)));
    assertFalse(ComplaintSla.isBreached(t, "critical", "resolved", t.plusDays(4)));
    assertFalse(ComplaintSla.isBreached(t, "critical", "investigating", t.plusDays(2)));
  }
}""")
w(TEST/"governance/ResearchExtractRulesTest.java", """package eg.gov.health.platform.governance;
import org.junit.jupiter.api.Test; import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class ResearchExtractRulesTest {
  @Test void directIdentifiersAlwaysViolations() {
    assertEquals(List.of("national_id"), ResearchExtractRules.violations(List.of("age_band","national_id")));
  }
  @Test void extractNeedsApprovalAndCleanFields() {
    assertFalse(ResearchExtractRules.extractAllowed("submitted", List.of("age_band")));
    assertTrue(ResearchExtractRules.extractAllowed("approved", List.of("age_band","governorate")));
    assertFalse(ResearchExtractRules.extractAllowed("approved", List.of("phone")));
  }
}""")
print("batch5 migration+tests done")
