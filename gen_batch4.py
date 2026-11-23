#!/usr/bin/env python3
"""Batch 4: radiology (module 93), surgery/WHO checklist (220), ambulance dispatch
(224), patient-safety incidents (module 156), infection-control precautions."""
import pathlib
BASE = pathlib.Path.home() / "workspace/egypt-health-v6/services/platform-api"
PKG = BASE / "src/main/java/eg/gov/health/platform"
RES = BASE / "src/main/resources"
TEST = BASE / "src/test/java/eg/gov/health/platform"
def w(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8"); print("wrote", path.name)

# ---------------- radiology (module 93 — no numbered cycle 217-254)
w(PKG/"diagnostics/RadiologyOrder.java", """package eg.gov.health.platform.diagnostics;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "radiology_orders")
public class RadiologyOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(nullable=false) private String modality; // XR, CT, MRI, US, MAMMO, ANGIO
  @Column(name="body_part") private String bodyPart;
  @Column(name="clinical_question", nullable=false) private String clinicalQuestion;
  @Column(nullable=false) private String priority = "routine";
  @Column(nullable=false) private String status = "ordered"; // ordered, scheduled, in_progress, imaged, reported, verified, cancelled
  @Column(name="report_text") private String reportText;
  @Column(name="reported_by") private String reportedBy;
  @Column(name="ordered_by") private String orderedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected RadiologyOrder() {}
  public RadiologyOrder(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String modality, String bodyPart, String clinicalQuestion, String priority, String orderedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.modality=modality; this.bodyPart=bodyPart; this.clinicalQuestion=clinicalQuestion;
    this.priority=priority==null?"routine":priority; this.orderedBy=orderedBy;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getFacilityId(){return facilityId;}
  public String getModality(){return modality;} public UUID getPatientId(){return patientId;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
  public void report(String text, String by){this.reportText=text; this.reportedBy=by; this.status="reported"; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"diagnostics/RadiologyOrderRepository.java", """package eg.gov.health.platform.diagnostics;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface RadiologyOrderRepository extends JpaRepository<RadiologyOrder, UUID> {
  List<RadiologyOrder> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  List<RadiologyOrder> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}""")
w(PKG/"diagnostics/RadiologyController.java", """package eg.gov.health.platform.diagnostics;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/radiology")
public class RadiologyController {
  private final RadiologyOrderRepository repo; private final AuditService audit;
  public RadiologyController(RadiologyOrderRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  static final List<String> MODALITIES = List.of("XR","CT","MRI","US","MAMMO","ANGIO");
  static final List<String> FLOW = List.of("ordered","scheduled","in_progress","imaged","reported","verified","cancelled");
  public record RadRequest(UUID patientId, UUID encounterId, String modality, String bodyPart, String clinicalQuestion, String priority) {}
  @PostMapping("/orders")
  Map<String,Object> create(@RequestBody RadRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||!MODALITIES.contains(r.modality()==null?"":r.modality()))
      throw BizException.badRequest("invalid_modality","وسيلة تصوير غير معروفة");
    if (r.clinicalQuestion()==null||r.clinicalQuestion().isBlank())
      throw BizException.badRequest("missing_clinical_question","السؤال السريري إلزامي — لا تصوير بلا مبرر (تبرير الإشعاع)");
    var o = repo.save(new RadiologyOrder(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.encounterId(), r.modality(), r.bodyPart(), r.clinicalQuestion(), r.priority(), p.userId()));
    audit.record("radiology.order.created","radiology_order",o.getId().toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  @PostMapping("/orders/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var o = repo.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","الطلب غير موجود"));
    String to = body.getOrDefault("status","");
    int cur = FLOW.indexOf(o.getStatus()); int nxt = FLOW.indexOf(to);
    if (nxt < 0) throw BizException.badRequest("invalid_status","حالة غير معروفة");
    if (!("cancelled".equals(to) && cur < FLOW.indexOf("verified")) && nxt != cur + 1)
      throw BizException.conflict("illegal_transition","المسار التسلسلي إلزامي: "+o.getStatus()+" -> "+to+" مرفوض");
    o.setStatus(to); repo.save(o);
    audit.record("radiology.order."+to,"radiology_order",id.toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  @PostMapping("/orders/{id}/report")
  Map<String,Object> report(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = TenantContext.get();
    var o = repo.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","الطلب غير موجود"));
    if (!"imaged".equals(o.getStatus())) throw BizException.conflict("not_imaged","التقرير بعد إتمام التصوير فقط");
    if (body.getOrDefault("report_text","").isBlank()) throw BizException.badRequest("empty_report","نص التقرير مطلوب");
    o.report(body.get("report_text"), p==null?null:p.userId()); repo.save(o);
    audit.record("radiology.order.reported","radiology_order",id.toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  @GetMapping("/orders")
  Map<String,Object> list(@RequestParam(required=false) UUID patientId) {
    var p = TenantContext.get();
    var rows = patientId!=null ? repo.findByPatientIdOrderByCreatedAtDesc(patientId) : repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"orders",rows,"count",rows.size());
  }
}""")

# ---------------- surgery + WHO checklist (cycle 220)
w(PKG/"clinical/WhoChecklist.java", """package eg.gov.health.platform.clinical;
import java.util.*;
/** WHO Surgical Safety Checklist gating (cycle 220): a case cannot move to
 *  the next stage until the current stage's items are ALL confirmed. The
 *  stages are sign_in -> time_out -> sign_out, and skipping is impossible
 *  by construction: completion of a stage is the only key to the next. */
public final class WhoChecklist {
  private WhoChecklist() {}
  public static final Map<String, List<String>> STAGES = Map.of(
    "sign_in", List.of("identity_confirmed","site_marked","consent_confirmed","anaesthesia_check","allergy_checked","airway_assessed"),
    "time_out", List.of("team_introduced","patient_site_procedure_confirmed","antibiotic_prophylaxis","imaging_displayed","anticipated_events_reviewed"),
    "sign_out", List.of("procedure_recorded","counts_correct","specimen_labelled","equipment_issues_noted","recovery_plan_reviewed"));
  public static final List<String> STAGE_ORDER = List.of("sign_in","time_out","sign_out");
  public static boolean stageComplete(String stage, Set<String> confirmedItems) {
    var required = STAGES.get(stage);
    return required != null && confirmedItems.containsAll(required);
  }
  public static Optional<String> nextStage(String currentStage, Set<String> confirmedItems) {
    int idx = STAGE_ORDER.indexOf(currentStage);
    if (idx < 0) return Optional.empty();
    if (!stageComplete(currentStage, confirmedItems)) return Optional.empty();
    return idx + 1 < STAGE_ORDER.size() ? Optional.of(STAGE_ORDER.get(idx + 1)) : Optional.of("completed");
  }
}""")
w(PKG/"clinical/SurgicalCase.java", """package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "surgical_cases")
public class SurgicalCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(name="procedure_name", nullable=false) private String procedureName;
  @Column(name="operating_room") private String operatingRoom;
  @Column(name="scheduled_at") private OffsetDateTime scheduledAt;
  @Column(nullable=false) private String checklistStage = "sign_in"; // sign_in, time_out, sign_out, completed
  @Column(name="confirmed_items") private String confirmedItems = "";
  @Column(nullable=false) private String status = "scheduled"; // scheduled, in_progress, completed, postponed, cancelled
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SurgicalCase() {}
  public SurgicalCase(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String procedureName, String operatingRoom, OffsetDateTime scheduledAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.procedureName=procedureName; this.operatingRoom=operatingRoom; this.scheduledAt=scheduledAt;
  }
  public UUID getId(){return id;} public String getChecklistStage(){return checklistStage;} public String getStatus(){return status;}
  public java.util.Set<String> confirmedSet() {
    return confirmedItems==null||confirmedItems.isBlank() ? java.util.Set.of() : java.util.Set.of(confirmedItems.split(","));
  }
  public void confirmItems(java.util.Collection<String> items) {
    var s = new java.util.TreeSet<>(confirmedSet()); s.addAll(items); this.confirmedItems = String.join(",", s); this.updatedAt=OffsetDateTime.now();
  }
  public void advanceTo(String stage){ this.checklistStage=stage; if ("completed".equals(stage)) this.status="completed"; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"clinical/SurgicalCaseRepository.java", """package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SurgicalCaseRepository extends JpaRepository<SurgicalCase, UUID> {
  List<SurgicalCase> findByFacilityIdOrderByScheduledAtAsc(UUID facilityId);
}""")
w(PKG/"clinical/SurgeryController.java", """package eg.gov.health.platform.clinical;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/surgery")
public class SurgeryController {
  private final SurgicalCaseRepository repo; private final AuditService audit;
  public SurgeryController(SurgicalCaseRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record CaseRequest(UUID patientId, UUID encounterId, String procedureName, String operatingRoom, OffsetDateTime scheduledAt) {}
  @PostMapping("/cases")
  Map<String,Object> create(@RequestBody CaseRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.procedureName()==null) throw BizException.badRequest("missing_field","patientId و procedureName مطلوبان");
    var c = repo.save(new SurgicalCase(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.encounterId(), r.procedureName(), r.operatingRoom(), r.scheduledAt()));
    audit.record("surgery.case.scheduled","surgical_case",c.getId().toString(),"{}");
    return Map.of("ok",true,"case",c);
  }
  @PostMapping("/cases/{id}/checklist/confirm")
  Map<String,Object> confirm(@PathVariable UUID id, @RequestBody Map<String,Object> body) {
    var c = repo.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","الحالة غير موجودة"));
    @SuppressWarnings("unchecked") var items = (List<String>) body.getOrDefault("items", List.of());
    var allowed = WhoChecklist.STAGES.getOrDefault(c.getChecklistStage(), List.of());
    var invalid = items.stream().filter(i -> !allowed.contains(i)).toList();
    if (!invalid.isEmpty()) throw BizException.badRequest("items_not_in_stage","عناصر لا تنتمي للمرحلة الحالية "+c.getChecklistStage()+": "+invalid);
    c.confirmItems(items); repo.save(c);
    audit.record("surgery.checklist.confirmed","surgical_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c,"stage_complete",WhoChecklist.stageComplete(c.getChecklistStage(), c.confirmedSet()));
  }
  @PostMapping("/cases/{id}/checklist/advance")
  Map<String,Object> advance(@PathVariable UUID id) {
    var c = repo.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","الحالة غير موجودة"));
    var next = WhoChecklist.nextStage(c.getChecklistStage(), c.confirmedSet());
    if (next.isEmpty()) throw BizException.conflict("checklist_incomplete","لا يمكن التقدم: عناصر مرحلة "+c.getChecklistStage()+" غير مكتملة (قائمة WHO، دورة 220)");
    c.advanceTo(next.get()); repo.save(c);
    audit.record("surgery.checklist.advanced","surgical_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c);
  }
  @GetMapping("/cases")
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByScheduledAtAsc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"cases",rows,"count",rows.size());
  }
}""")

# ---------------- ambulance dispatch (cycle 224): nearest-unit pure logic
w(PKG/"emergency/DispatchMath.java", """package eg.gov.health.platform.emergency;
import java.util.*;
/** Nearest-available-unit selection (cycle 224 — referral & medical transport). Distance is haversine;
 *  only units in status 'available' with the required capability qualify.
 *  Deterministic tie-break by unit code keeps dispatch decisions auditable. */
public final class DispatchMath {
  private DispatchMath() {}
  public record UnitView(String unitCode, double lat, double lon, String status, Set<String> capabilities) {}
  public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    double r = 6371.0;
    double dLat = Math.toRadians(lat2-lat1), dLon = Math.toRadians(lon2-lon1);
    double a = Math.sin(dLat/2)*Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dLon/2)*Math.sin(dLon/2);
    return 2 * r * Math.asin(Math.sqrt(a));
  }
  public static Optional<UnitView> nearestAvailable(double lat, double lon, String requiredCapability, List<UnitView> units) {
    return units.stream()
      .filter(u -> "available".equals(u.status()))
      .filter(u -> requiredCapability == null || u.capabilities().contains(requiredCapability))
      .min(Comparator.comparingDouble((UnitView u) -> haversineKm(lat, lon, u.lat(), u.lon())).thenComparing(UnitView::unitCode));
  }
}""")
w(PKG/"emergency/AmbulanceUnit.java", """package eg.gov.health.platform.emergency;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "ambulance_units")
public class AmbulanceUnit {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="unit_code", nullable=false, unique=true) private String unitCode;
  @Column(nullable=false) private Double lat; @Column(nullable=false) private Double lon;
  @Column(nullable=false) private String status = "available"; // available, dispatched, on_scene, transporting, returning, offline
  @Column private String capabilities = ""; // comma list: ALS, BLS, NEONATAL, ICU
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AmbulanceUnit() {}
  public AmbulanceUnit(UUID facilityId, String governorate, String unitCode, Double lat, Double lon, String capabilities) {
    this.facilityId=facilityId; this.governorate=governorate; this.unitCode=unitCode; this.lat=lat; this.lon=lon; this.capabilities=capabilities==null?"":capabilities;
  }
  public UUID getId(){return id;} public String getUnitCode(){return unitCode;} public String getStatus(){return status;}
  public Double getLat(){return lat;} public Double getLon(){return lon;}
  public java.util.Set<String> capabilitySet(){ return capabilities==null||capabilities.isBlank()? java.util.Set.of() : java.util.Set.of(capabilities.split(",")); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
  public void moveTo(Double la, Double lo){this.lat=la; this.lon=lo; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"emergency/DispatchRequest.java", """package eg.gov.health.platform.emergency;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "dispatch_requests")
public class DispatchRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id") private UUID patientId;
  @Column(nullable=false) private Double lat; @Column(nullable=false) private Double lon;
  @Column(nullable=false) private String complaint;
  @Column(name="required_capability") private String requiredCapability;
  @Column(name="assigned_unit_id") private UUID assignedUnitId;
  @Column(nullable=false) private String status = "received"; // received, assigned, en_route, on_scene, transporting, completed, cancelled
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DispatchRequest() {}
  public DispatchRequest(UUID facilityId, String governorate, UUID patientId, Double lat, Double lon, String complaint, String requiredCapability) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.lat=lat; this.lon=lon;
    this.complaint=complaint; this.requiredCapability=requiredCapability;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void assign(UUID unitId){this.assignedUnitId=unitId; this.status="assigned"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"emergency/AmbulanceUnitRepository.java", """package eg.gov.health.platform.emergency;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AmbulanceUnitRepository extends JpaRepository<AmbulanceUnit, UUID> {
  List<AmbulanceUnit> findByStatus(String status);
  Optional<AmbulanceUnit> findByUnitCode(String unitCode);
}""")
w(PKG/"emergency/DispatchRequestRepository.java", """package eg.gov.health.platform.emergency;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DispatchRequestRepository extends JpaRepository<DispatchRequest, UUID> {
  List<DispatchRequest> findByStatusOrderByCreatedAtAsc(String status);
}""")
w(PKG/"emergency/DispatchController.java", """package eg.gov.health.platform.emergency;
import eg.gov.health.platform.common.*;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/dispatch")
public class DispatchController {
  private final AmbulanceUnitRepository units; private final DispatchRequestRepository requests; private final AuditService audit;
  public DispatchController(AmbulanceUnitRepository units, DispatchRequestRepository requests, AuditService audit) {
    this.units=units; this.requests=requests; this.audit=audit;
  }
  public record UnitReg(String unitCode, Double lat, Double lon, String capabilities) {}
  @PostMapping("/units")
  Map<String,Object> registerUnit(@RequestBody UnitReg r) {
    var p = TenantContext.get();
    if (r.unitCode()==null||r.lat()==null||r.lon()==null) throw BizException.badRequest("missing_field","unitCode و lat و lon مطلوبة");
    var u = units.save(new AmbulanceUnit(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null, p==null?null:p.governorate(), r.unitCode(), r.lat(), r.lon(), r.capabilities()));
    return Map.of("ok",true,"unit",u);
  }
  public record DispatchReq(UUID patientId, Double lat, Double lon, String complaint, String requiredCapability) {}
  @PostMapping("/requests") @Transactional
  Map<String,Object> request(@RequestBody DispatchReq r) {
    var p = TenantContext.get();
    if (r.lat()==null||r.lon()==null||r.complaint()==null) throw BizException.badRequest("missing_field","lat و lon و complaint مطلوبة");
    var req = requests.save(new DispatchRequest(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.lat(), r.lon(), r.complaint(), r.requiredCapability()));
    var views = units.findByStatus("available").stream()
      .map(u -> new DispatchMath.UnitView(u.getUnitCode(), u.getLat(), u.getLon(), u.getStatus(), u.capabilitySet())).toList();
    var nearest = DispatchMath.nearestAvailable(r.lat(), r.lon(), r.requiredCapability(), views);
    var out = new HashMap<String,Object>(); out.put("ok",true); out.put("request",req);
    if (nearest.isPresent()) {
      var unit = units.findByUnitCode(nearest.get().unitCode()).orElseThrow();
      req.assign(unit.getId()); unit.setStatus("dispatched"); units.save(unit); requests.save(req);
      out.put("assigned_unit", unit.getUnitCode());
      out.put("distance_km", Math.round(DispatchMath.haversineKm(r.lat(), r.lon(), unit.getLat(), unit.getLon())*10.0)/10.0);
      audit.record("dispatch.assigned","dispatch_request",req.getId().toString(),"{}");
    } else {
      out.put("warning","لا توجد وحدة متاحة بالقدرة المطلوبة — يبقى الطلب في قائمة الانتظار للتصعيد (دورة 224)");
      audit.record("dispatch.unassigned","dispatch_request",req.getId().toString(),"{}");
    }
    return out;
  }
  @PostMapping("/requests/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var req = requests.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    String to = body.getOrDefault("status","");
    Map<String,List<String>> flow = Map.of(
      "received", List.of("assigned","cancelled"), "assigned", List.of("en_route","cancelled"),
      "en_route", List.of("on_scene"), "on_scene", List.of("transporting","completed"),
      "transporting", List.of("completed"), "completed", List.of(), "cancelled", List.of());
    if (!flow.getOrDefault(req.getStatus(), List.of()).contains(to))
      throw BizException.conflict("illegal_transition","انتقال غير مسموح: "+req.getStatus()+" -> "+to);
    req.setStatus(to); requests.save(req);
    audit.record("dispatch."+to,"dispatch_request",id.toString(),"{}");
    return Map.of("ok",true,"request",req);
  }
  @GetMapping("/queue")
  Map<String,Object> queue() {
    var rows = requests.findByStatusOrderByCreatedAtAsc("received");
    return Map.of("ok",true,"queue",rows,"count",rows.size());
  }
}""")

# ---------------- incidents (module 156 — no numbered cycle 217-254) + infection control
w(PKG/"quality/IncidentReport.java", """package eg.gov.health.platform.quality;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "incident_reports")
public class IncidentReport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id") private UUID patientId;
  @Column(nullable=false) private String category; // fall, medication, pressure_ulcer, infection, equipment, other
  @Column(nullable=false) private String severity; // near_miss, minor, moderate, major, catastrophic
  @Column(nullable=false) private String description;
  @Column(name="reported_by") private String reportedBy;
  @Column(nullable=false) private String status = "reported"; // reported, under_review, action_assigned, closed
  @Column(name="root_cause") private String rootCause;
  @Column(name="corrective_action") private String correctiveAction;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected IncidentReport() {}
  public IncidentReport(UUID facilityId, String governorate, UUID patientId, String category, String severity, String description, String reportedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.category=category;
    this.severity=severity; this.description=description; this.reportedBy=reportedBy;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getSeverity(){return severity;}
  public void review(String rootCause, String action){ this.rootCause=rootCause; this.correctiveAction=action; this.status="action_assigned"; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"quality/IncidentReportRepository.java", """package eg.gov.health.platform.quality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface IncidentReportRepository extends JpaRepository<IncidentReport, UUID> {
  List<IncidentReport> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  List<IncidentReport> findByFacilityIdAndStatus(UUID facilityId, String status);
}""")
w(PKG/"quality/IncidentController.java", """package eg.gov.health.platform.quality;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/incidents")
public class IncidentController {
  private final IncidentReportRepository repo; private final AuditService audit;
  public IncidentController(IncidentReportRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  static final List<String> SEVERITIES = List.of("near_miss","minor","moderate","major","catastrophic");
  public record IncidentRequest(UUID patientId, String category, String severity, String description) {}
  @PostMapping
  Map<String,Object> report(@RequestBody IncidentRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.category()==null||!SEVERITIES.contains(r.severity()==null?"":r.severity())||r.description()==null||r.description().isBlank())
      throw BizException.badRequest("invalid_incident","فئة وخطورة معروفة ووصف مطلوبة");
    var inc = repo.save(new IncidentReport(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.category(), r.severity(), r.description(), p.userId()));
    audit.record("incident.reported","incident",inc.getId().toString(),"{}");
    var out = new HashMap<String,Object>(); out.put("ok",true); out.put("incident",inc);
    if (List.of("major","catastrophic").contains(r.severity()))
      out.put("escalation","حدث جسيم — إخطار فوري لإدارة الجودة والسلامة ومدير المنشأة");
    return out;
  }
  @PostMapping("/{id}/review")
  Map<String,Object> review(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var inc = repo.findById(id).orElseThrow(() -> BizException.notFound("incident_not_found","البلاغ غير موجود"));
    if (body.getOrDefault("root_cause","").isBlank() || body.getOrDefault("corrective_action","").isBlank())
      throw BizException.badRequest("review_incomplete","تحليل السبب الجذري والإجراء التصحيحي إلزاميان قبل التقدم");
    inc.review(body.get("root_cause"), body.get("corrective_action")); repo.save(inc);
    audit.record("incident.reviewed","incident",id.toString(),"{}");
    return Map.of("ok",true,"incident",inc);
  }
  @PostMapping("/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    var inc = repo.findById(id).orElseThrow(() -> BizException.notFound("incident_not_found","البلاغ غير موجود"));
    if (!"action_assigned".equals(inc.getStatus())) throw BizException.conflict("not_reviewed","لا يُغلق بلاغ قبل المراجعة وتعيين إجراء تصحيحي");
    inc.close(); repo.save(inc);
    audit.record("incident.closed","incident",id.toString(),"{}");
    return Map.of("ok",true,"incident",inc);
  }
  @GetMapping
  Map<String,Object> list(@RequestParam(required=false) String status) {
    var p = TenantContext.get();
    var rows = status==null ? repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()))
                            : repo.findByFacilityIdAndStatus(UUID.fromString(p.facilityId()), status);
    return Map.of("ok",true,"incidents",rows,"count",rows.size());
  }
}""")
w(PKG/"quality/IsolationPrecautions.java", """package eg.gov.health.platform.quality;
import java.util.*;
/** Infection-control precaution mapping: condition -> required precaution
 *  level (standard / contact / droplet / airborne). Airborne conditions
 *  additionally require a negative-pressure room flag for bed assignment. */
public final class IsolationPrecautions {
  private IsolationPrecautions() {}
  static final Map<String,String> MAP = Map.ofEntries(
    Map.entry("MRSA","contact"), Map.entry("C_DIFF","contact"), Map.entry("VRE","contact"),
    Map.entry("INFLUENZA","droplet"), Map.entry("MENINGOCOCCAL","droplet"), Map.entry("COVID19","droplet"),
    Map.entry("TB","airborne"), Map.entry("MEASLES","airborne"), Map.entry("VARICELLA","airborne"));
  public static String precautionFor(String conditionCode) {
    if (conditionCode == null) return "standard";
    return MAP.getOrDefault(conditionCode.toUpperCase(), "standard");
  }
  public static boolean requiresNegativePressure(String conditionCode) { return "airborne".equals(precautionFor(conditionCode)); }
}""")
print("batch4 java done")

# ---------------- Flyway V5 + tests
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
v5 = """-- V5 — Batch 4 (Catalog v6): radiology (module 93), surgery 220, dispatch 224, incidents (module 156).
CREATE TABLE IF NOT EXISTS radiology_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), encounter_id UUID REFERENCES encounters(id),
  modality TEXT NOT NULL CHECK (modality IN ('XR','CT','MRI','US','MAMMO','ANGIO')),
  body_part TEXT, clinical_question TEXT NOT NULL,
  priority TEXT NOT NULL DEFAULT 'routine',
  status TEXT NOT NULL DEFAULT 'ordered' CHECK (status IN ('ordered','scheduled','in_progress','imaged','reported','verified','cancelled')),
  report_text TEXT, reported_by TEXT, ordered_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_rad_orders_facility_status ON radiology_orders (facility_id, status);
CREATE TABLE IF NOT EXISTS surgical_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), encounter_id UUID REFERENCES encounters(id),
  procedure_name TEXT NOT NULL, operating_room TEXT, scheduled_at TIMESTAMPTZ,
  checklist_stage TEXT NOT NULL DEFAULT 'sign_in' CHECK (checklist_stage IN ('sign_in','time_out','sign_out','completed')),
  confirmed_items TEXT NOT NULL DEFAULT '',
  status TEXT NOT NULL DEFAULT 'scheduled' CHECK (status IN ('scheduled','in_progress','completed','postponed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_surgical_facility ON surgical_cases (facility_id, scheduled_at);
CREATE TABLE IF NOT EXISTS ambulance_units (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  unit_code TEXT NOT NULL UNIQUE, lat DOUBLE PRECISION NOT NULL, lon DOUBLE PRECISION NOT NULL,
  status TEXT NOT NULL DEFAULT 'available' CHECK (status IN ('available','dispatched','on_scene','transporting','returning','offline')),
  capabilities TEXT NOT NULL DEFAULT '',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS dispatch_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID REFERENCES patients(id),
  lat DOUBLE PRECISION NOT NULL, lon DOUBLE PRECISION NOT NULL, complaint TEXT NOT NULL,
  required_capability TEXT, assigned_unit_id UUID REFERENCES ambulance_units(id),
  status TEXT NOT NULL DEFAULT 'received' CHECK (status IN ('received','assigned','en_route','on_scene','transporting','completed','cancelled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_dispatch_status ON dispatch_requests (status, created_at);
CREATE TABLE IF NOT EXISTS incident_reports (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID REFERENCES patients(id),
  category TEXT NOT NULL, severity TEXT NOT NULL CHECK (severity IN ('near_miss','minor','moderate','major','catastrophic')),
  description TEXT NOT NULL, reported_by TEXT,
  status TEXT NOT NULL DEFAULT 'reported' CHECK (status IN ('reported','under_review','action_assigned','closed')),
  root_cause TEXT, corrective_action TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_incidents_facility_status ON incident_reports (facility_id, status);
""" + "\n".join(rls_fac.format(t=t) for t in ["radiology_orders","surgical_cases","incident_reports"]) + """
GRANT SELECT, INSERT, UPDATE, DELETE ON radiology_orders, surgical_cases, ambulance_units, dispatch_requests, incident_reports TO app_user;
"""
w(RES/"db/migration/V5__radiology_surgery_dispatch_incidents.sql", v5)

w(TEST/"clinical/WhoChecklistTest.java", """package eg.gov.health.platform.clinical;
import org.junit.jupiter.api.Test; import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class WhoChecklistTest {
  @Test void stageNeedsEveryItem() {
    assertFalse(WhoChecklist.stageComplete("sign_in", Set.of("identity_confirmed","site_marked")));
    assertTrue(WhoChecklist.stageComplete("sign_in",
      Set.of("identity_confirmed","site_marked","consent_confirmed","anaesthesia_check","allergy_checked","airway_assessed")));
  }
  @Test void cannotAdvanceIncomplete() { assertTrue(WhoChecklist.nextStage("sign_in", Set.of()).isEmpty()); }
  @Test void advanceAfterCompletion() {
    var all = Set.copyOf(WhoChecklist.STAGES.get("sign_in"));
    assertEquals("time_out", WhoChecklist.nextStage("sign_in", all).orElseThrow());
  }
}""")
w(TEST/"emergency/DispatchMathTest.java", """package eg.gov.health.platform.emergency;
import org.junit.jupiter.api.Test; import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class DispatchMathTest {
  DispatchMath.UnitView u(String code, double la, double lo, String st, String... caps) {
    return new DispatchMath.UnitView(code, la, lo, st, Set.of(caps));
  }
  @Test void cairoAlexandriaDistance() {
    double d = DispatchMath.haversineKm(30.0444, 31.2357, 31.2001, 29.9187);
    assertTrue(d > 175 && d < 190, "was " + d);
  }
  @Test void nearestAvailableWins() {
    var units = List.of(u("FAR", 30.20, 31.40, "available","ALS"), u("NEAR", 30.05, 31.24, "available","ALS"), u("BUSY", 30.045, 31.236, "dispatched","ALS"));
    var pick = DispatchMath.nearestAvailable(30.0444, 31.2357, "ALS", units).orElseThrow();
    assertEquals("NEAR", pick.unitCode());
  }
  @Test void capabilityFilters() {
    var units = List.of(u("BLS1", 30.05, 31.24, "available","BLS"));
    assertTrue(DispatchMath.nearestAvailable(30.0444, 31.2357, "NEONATAL", units).isEmpty());
  }
}""")
w(TEST/"quality/IsolationPrecautionsTest.java", """package eg.gov.health.platform.quality;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class IsolationPrecautionsTest {
  @Test void mapping() {
    assertEquals("airborne", IsolationPrecautions.precautionFor("TB"));
    assertEquals("contact", IsolationPrecautions.precautionFor("MRSA"));
    assertEquals("droplet", IsolationPrecautions.precautionFor("INFLUENZA"));
    assertEquals("standard", IsolationPrecautions.precautionFor("CELLULITIS"));
    assertTrue(IsolationPrecautions.requiresNegativePressure("MEASLES"));
    assertFalse(IsolationPrecautions.requiresNegativePressure("MRSA"));
  }
}""")
print("batch4 migration+tests done")
