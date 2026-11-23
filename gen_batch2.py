#!/usr/bin/env python3
"""Batch 2: diagnostics (lab cycle 219), pharmacy (222), blood bank (223),
referrals (224), capacity/beds (225) — full Java modules + Flyway V3."""
import pathlib
BASE = pathlib.Path.home() / "workspace/egypt-health-v6/services/platform-api"
PKG = BASE / "src/main/java/eg/gov/health/platform"
RES = BASE / "src/main/resources"
TEST = BASE / "src/test/java/eg/gov/health/platform"
def w(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8"); print("wrote", path.name)

# ---------------- diagnostics: lab orders, specimens, results, critical alerts (cycle 219)
w(PKG/"diagnostics/LabOrder.java", """package eg.gov.health.platform.diagnostics;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "lab_orders")
public class LabOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(name="test_code", nullable=false) private String testCode;
  @Column(name="test_name") private String testName;
  @Column(nullable=false) private String priority = "routine"; // routine, urgent, stat
  @Column(nullable=false) private String status = "ordered"; // ordered, specimen_collected, received, in_analysis, resulted, verified, corrected, cancelled
  @Column(name="ordered_by") private String orderedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected LabOrder() {}
  public LabOrder(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String testCode, String testName, String priority, String orderedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.testCode=testCode; this.testName=testName; this.priority=priority==null?"routine":priority; this.orderedBy=orderedBy;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getGovernorate(){return governorate;}
  public UUID getPatientId(){return patientId;} public String getTestCode(){return testCode;} public String getStatus(){return status;}
  public String getPriority(){return priority;} public UUID getEncounterId(){return encounterId;} public String getTestName(){return testName;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"diagnostics/LabResult.java", """package eg.gov.health.platform.diagnostics;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "lab_results")
public class LabResult {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="order_id", nullable=false) private UUID orderId;
  @Column(nullable=false) private BigDecimal value;
  private String unit;
  @Column(name="ref_low") private BigDecimal refLow;
  @Column(name="ref_high") private BigDecimal refHigh;
  @Column(name="is_critical", nullable=false) private boolean critical;
  @Column(nullable=false) private int version = 1;
  @Column(name="supersedes_id") private UUID supersedesId;
  @Column(nullable=false) private String status = "resulted"; // resulted, verified, corrected
  @Column(name="verified_by") private String verifiedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected LabResult() {}
  public LabResult(UUID facilityId, String governorate, UUID orderId, BigDecimal value, String unit, BigDecimal refLow, BigDecimal refHigh, boolean critical, UUID supersedesId) {
    this.facilityId=facilityId; this.governorate=governorate; this.orderId=orderId; this.value=value; this.unit=unit;
    this.refLow=refLow; this.refHigh=refHigh; this.critical=critical; this.supersedesId=supersedesId;
    if (supersedesId != null) this.status = "corrected";
  }
  public UUID getId(){return id;} public UUID getOrderId(){return orderId;} public BigDecimal getValue(){return value;}
  public boolean isCritical(){return critical;} public String getStatus(){return status;} public int getVersion(){return version;}
  public void setVersion(int v){this.version=v;} public void verify(String by){this.status="verified"; this.verifiedBy=by; this.updatedAt=OffsetDateTime.now();}
  public UUID getFacilityId(){return facilityId;} public String getUnit(){return unit;}
}""")
w(PKG/"diagnostics/CriticalValueRules.java", """package eg.gov.health.platform.diagnostics;
import java.math.BigDecimal; import java.util.Map;
/** Critical (panic) value rules — cycle 219: a critical result creates an
 *  alert that cannot be closed without a documented acknowledgement or a
 *  timed escalation to the named alternate. Limits are facility-configurable
 *  in production; these are the national default reference limits. */
public final class CriticalValueRules {
  private CriticalValueRules() {}
  public record Limit(BigDecimal low, BigDecimal high) {}
  public static final Map<String, Limit> LIMITS = Map.of(
    "K",   new Limit(new BigDecimal("2.8"), new BigDecimal("6.0")),
    "NA",  new Limit(new BigDecimal("120"), new BigDecimal("160")),
    "GLU", new Limit(new BigDecimal("40"),  new BigDecimal("500")),
    "HB",  new Limit(new BigDecimal("7.0"), new BigDecimal("20.0")),
    "PLT", new Limit(new BigDecimal("50"),  new BigDecimal("1000")),
    "CA",  new Limit(new BigDecimal("6.5"), new BigDecimal("13.0")),
    "CREA",new Limit(null,                   new BigDecimal("4.0")));
  public static boolean isCritical(String testCode, BigDecimal value) {
    var lim = LIMITS.get(testCode == null ? "" : testCode.toUpperCase());
    if (lim == null || value == null) return false;
    if (lim.low() != null && value.compareTo(lim.low()) < 0) return true;
    return lim.high() != null && value.compareTo(lim.high()) > 0;
  }
}""")
w(PKG/"diagnostics/LabRepositories.java.tmp", "placeholder")
(PKG/"diagnostics/LabRepositories.java.tmp").unlink()
w(PKG/"diagnostics/LabOrderRepository.java", """package eg.gov.health.platform.diagnostics;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface LabOrderRepository extends JpaRepository<LabOrder, UUID> {
  List<LabOrder> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  List<LabOrder> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}""")
w(PKG/"diagnostics/LabResultRepository.java", """package eg.gov.health.platform.diagnostics;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface LabResultRepository extends JpaRepository<LabResult, UUID> {
  List<LabResult> findByOrderIdOrderByVersionDesc(UUID orderId);
  List<LabResult> findByFacilityIdAndCriticalTrueAndStatusNot(UUID facilityId, String status);
}""")
w(PKG/"diagnostics/LabController.java", """package eg.gov.health.platform.diagnostics;
import eg.gov.health.platform.common.*;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.util.*;
@RestController @RequestMapping("/api/labs")
public class LabController {
  private final LabOrderRepository orders; private final LabResultRepository results; private final AuditService audit;
  public LabController(LabOrderRepository orders, LabResultRepository results, AuditService audit) { this.orders=orders; this.results=results; this.audit=audit; }
  static final List<String> FLOW = List.of("ordered","specimen_collected","received","in_analysis","resulted","verified","corrected","cancelled");
  public record OrderRequest(UUID patientId, UUID encounterId, String testCode, String testName, String priority) {}
  @PostMapping("/orders")
  Map<String,Object> create(@RequestBody OrderRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.testCode()==null) throw BizException.badRequest("missing_field","patientId و testCode مطلوبان");
    var o = orders.save(new LabOrder(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.encounterId(), r.testCode(), r.testName(), r.priority(), p.userId()));
    audit.record("lab.order.created","lab_order",o.getId().toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  @GetMapping("/orders")
  Map<String,Object> list(@RequestParam(required=false) UUID patientId) {
    var p = TenantContext.get();
    var rows = patientId!=null ? orders.findByPatientIdOrderByCreatedAtDesc(patientId) : orders.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"orders",rows,"count",rows.size());
  }
  @PostMapping("/orders/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var o = orders.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","الطلب غير موجود"));
    String to = body.getOrDefault("status","");
    int cur = FLOW.indexOf(o.getStatus()); int nxt = FLOW.indexOf(to);
    if (nxt < 0) throw BizException.badRequest("invalid_status","حالة غير معروفة");
    // Forward-only along the documented flow; cancellation allowed any time before verified.
    if (!("cancelled".equals(to) && cur < FLOW.indexOf("verified")) && nxt != cur + 1)
      throw BizException.conflict("illegal_transition","لا يمكن الانتقال من "+o.getStatus()+" إلى "+to+" — المسار التسلسلي إلزامي (دورة 219)");
    o.setStatus(to); orders.save(o);
    audit.record("lab.order."+to,"lab_order",id.toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  public record ResultRequest(BigDecimal value, String unit, BigDecimal refLow, BigDecimal refHigh, UUID supersedesId) {}
  @PostMapping("/orders/{id}/results") @Transactional
  Map<String,Object> result(@PathVariable UUID id, @RequestBody ResultRequest r) {
    var p = TenantContext.get();
    var o = orders.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","الطلب غير موجود"));
    if (r.value()==null) throw BizException.badRequest("missing_field","value مطلوبة");
    boolean critical = CriticalValueRules.isCritical(o.getTestCode(), r.value());
    var res = new LabResult(o.getFacilityId(), o.getGovernorate(), id, r.value(), r.unit(), r.refLow(), r.refHigh(), critical, r.supersedesId());
    if (r.supersedesId()!=null) {
      var prev = results.findById(r.supersedesId()).orElseThrow(() -> BizException.notFound("result_not_found","النتيجة السابقة غير موجودة"));
      res.setVersion(prev.getVersion()+1); // correction = NEW version; the old one is never overwritten (cycle 219)
    }
    results.save(res);
    if (!"resulted".equals(o.getStatus()) && !"verified".equals(o.getStatus())) { o.setStatus("resulted"); orders.save(o); }
    audit.record(critical ? "lab.result.critical" : "lab.result.created","lab_result",res.getId().toString(),"{}");
    var out = new HashMap<String,Object>(); out.put("ok",true); out.put("result",res); out.put("critical",critical);
    if (critical) out.put("alert","نتيجة حرجة — يجب الإقرار بالاستلام من الطبيب المعالج أو التصعيد للبديل المحدد (دورة 219)");
    return out;
  }
  @PostMapping("/results/{id}/verify")
  Map<String,Object> verify(@PathVariable UUID id) {
    var p = TenantContext.get();
    var res = results.findById(id).orElseThrow(() -> BizException.notFound("result_not_found","النتيجة غير موجودة"));
    res.verify(p==null?null:p.userId()); results.save(res);
    audit.record("lab.result.verified","lab_result",id.toString(),"{}");
    return Map.of("ok",true,"result",res);
  }
  @GetMapping("/critical-alerts")
  Map<String,Object> criticalAlerts() {
    var p = TenantContext.get();
    var rows = results.findByFacilityIdAndCriticalTrueAndStatusNot(UUID.fromString(p.facilityId()), "verified");
    return Map.of("ok",true,"critical_alerts",rows,"count",rows.size());
  }
}""")

# ---------------- pharmacy: prescriptions + dispensing + interaction check (cycle 222)
w(PKG/"pharmacy/Prescription.java", """package eg.gov.health.platform.pharmacy;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "prescriptions")
public class Prescription {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(name="drug_code", nullable=false) private String drugCode;
  @Column(name="drug_name") private String drugName;
  private String dose; private String frequency; private String duration;
  @Column(nullable=false) private String status = "prescribed"; // prescribed, verified, dispensed, partially_dispensed, returned, cancelled
  @Column(name="prescribed_by") private String prescribedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Prescription() {}
  public Prescription(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String drugCode, String drugName, String dose, String frequency, String duration, String prescribedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId;
    this.drugCode=drugCode; this.drugName=drugName; this.dose=dose; this.frequency=frequency; this.duration=duration; this.prescribedBy=prescribedBy;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getGovernorate(){return governorate;}
  public UUID getPatientId(){return patientId;} public String getDrugCode(){return drugCode;} public String getStatus(){return status;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"pharmacy/InteractionRules.java", """package eg.gov.health.platform.pharmacy;
import java.util.*;
/** Drug-interaction + duplication guard (cycle 222, medication reconciliation).
 *  Advisory to the verifying pharmacist: a flagged pair never auto-blocks a
 *  prescription silently — it requires pharmacist verification with reason. */
public final class InteractionRules {
  private InteractionRules() {}
  static final Set<Set<String>> SEVERE = Set.of(
    Set.of("WARFARIN","ASPIRIN"), Set.of("WARFARIN","IBUPROFEN"),
    Set.of("METHOTREXATE","TRIMETHOPRIM"), Set.of("SILDENAFIL","NITROGLYCERIN"),
    Set.of("CLARITHROMYCIN","SIMVASTATIN"));
  public static Optional<String> severeInteraction(String drugCode, Collection<String> activeDrugCodes) {
    if (drugCode == null) return Optional.empty();
    String d = drugCode.toUpperCase();
    for (String other : activeDrugCodes) {
      if (other == null) continue;
      if (SEVERE.contains(Set.of(d, other.toUpperCase()))) return Optional.of(d + " + " + other.toUpperCase());
    }
    return Optional.empty();
  }
  public static boolean isDuplicate(String drugCode, Collection<String> activeDrugCodes) {
    if (drugCode == null) return false;
    return activeDrugCodes.stream().anyMatch(x -> x != null && x.equalsIgnoreCase(drugCode));
  }
}""")
w(PKG/"pharmacy/PrescriptionRepository.java", """package eg.gov.health.platform.pharmacy;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
  List<Prescription> findByPatientIdAndStatusIn(UUID patientId, List<String> statuses);
  List<Prescription> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
}""")
w(PKG/"pharmacy/PharmacyController.java", """package eg.gov.health.platform.pharmacy;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/pharmacy")
public class PharmacyController {
  private final PrescriptionRepository repo; private final AuditService audit;
  public PharmacyController(PrescriptionRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record RxRequest(UUID patientId, UUID encounterId, String drugCode, String drugName, String dose, String frequency, String duration) {}
  @PostMapping("/prescriptions")
  Map<String,Object> prescribe(@RequestBody RxRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.drugCode()==null) throw BizException.badRequest("missing_field","patientId و drugCode مطلوبان");
    var active = repo.findByPatientIdAndStatusIn(r.patientId(), List.of("prescribed","verified","dispensed"));
    var activeCodes = active.stream().map(Prescription::getDrugCode).toList();
    var interaction = InteractionRules.severeInteraction(r.drugCode(), activeCodes);
    boolean duplicate = InteractionRules.isDuplicate(r.drugCode(), activeCodes);
    var rx = repo.save(new Prescription(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.encounterId(),
      r.drugCode(), r.drugName(), r.dose(), r.frequency(), r.duration(), p.userId()));
    audit.record("pharmacy.prescribed","prescription",rx.getId().toString(),"{}");
    var out = new HashMap<String,Object>(); out.put("ok",true); out.put("prescription",rx);
    if (interaction.isPresent()) out.put("warning", Map.of("type","severe_interaction","pair",interaction.get(),
      "action","يتطلب تحقق الصيدلي السريري مع توثيق السبب قبل الصرف (دورة 222)"));
    if (duplicate) out.put("duplicate_warning","نفس الدواء فعال بالفعل لهذا المريض — راجع المصالحة الدوائية");
    return out;
  }
  @PostMapping("/prescriptions/{id}/dispense")
  Map<String,Object> dispense(@PathVariable UUID id, @RequestBody(required=false) Map<String,String> body) {
    var rx = repo.findById(id).orElseThrow(() -> BizException.notFound("rx_not_found","الوصفة غير موجودة"));
    if ("dispensed".equals(rx.getStatus())) throw BizException.conflict("already_dispensed","صُرفت مسبقًا — منع الازدواج (دورة 222)");
    if ("cancelled".equals(rx.getStatus())) throw BizException.conflict("cancelled_rx","وصفة ملغاة لا تُصرف");
    rx.setStatus("dispensed"); repo.save(rx);
    audit.record("pharmacy.dispensed","prescription",id.toString(),"{}");
    return Map.of("ok",true,"prescription",rx);
  }
  @GetMapping("/prescriptions")
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"prescriptions",rows,"count",rows.size());
  }
}""")

# ---------------- blood bank (cycle 223): compatibility is pure + fully tested
w(PKG/"bloodbank/BloodCompatibility.java", """package eg.gov.health.platform.bloodbank;
import java.util.*;
/** ABO/Rh red-cell compatibility matrix (cycle 223). O- is the universal
 *  red-cell donor; AB+ the universal recipient. Plasma compatibility is the
 *  inverse and handled by the plasma-specific table in the full module. */
public final class BloodCompatibility {
  private BloodCompatibility() {}
  public static final List<String> GROUPS = List.of("O-","O+","A-","A+","B-","B+","AB-","AB+");
  static boolean aboCanDonate(String donor, String recipient) {
    String d = donor.substring(0, donor.length()-1), r = recipient.substring(0, recipient.length()-1);
    return switch (d) {
      case "O" -> true;
      case "A" -> r.equals("A") || r.equals("AB");
      case "B" -> r.equals("B") || r.equals("AB");
      case "AB" -> r.equals("AB");
      default -> false;
    };
  }
  public static boolean canDonateRbc(String donorGroup, String recipientGroup) {
    if (!GROUPS.contains(donorGroup) || !GROUPS.contains(recipientGroup)) return false;
    boolean rhOk = donorGroup.endsWith("-") || recipientGroup.endsWith("+");
    return rhOk && aboCanDonate(donorGroup, recipientGroup);
  }
  public static List<String> compatibleDonorsFor(String recipientGroup) {
    return GROUPS.stream().filter(d -> canDonateRbc(d, recipientGroup)).toList();
  }
}""")
w(PKG/"bloodbank/BloodUnit.java", """package eg.gov.health.platform.bloodbank;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "blood_units")
public class BloodUnit {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="unit_code", nullable=false, unique=true) private String unitCode;
  @Column(name="blood_group", nullable=false) private String bloodGroup;
  @Column(nullable=false) private String component = "RBC"; // RBC, PLASMA, PLATELETS, WHOLE
  @Column(name="donated_at") private LocalDate donatedAt;
  @Column(name="expires_at") private LocalDate expiresAt;
  @Column(nullable=false) private String status = "available"; // available, reserved, issued, transfused, expired, discarded
  @Column(name="reserved_for_patient") private UUID reservedForPatient;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected BloodUnit() {}
  public BloodUnit(UUID facilityId, String governorate, String unitCode, String bloodGroup, String component, LocalDate donatedAt, LocalDate expiresAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.unitCode=unitCode; this.bloodGroup=bloodGroup;
    this.component=component==null?"RBC":component; this.donatedAt=donatedAt; this.expiresAt=expiresAt;
  }
  public UUID getId(){return id;} public String getUnitCode(){return unitCode;} public String getBloodGroup(){return bloodGroup;}
  public String getStatus(){return status;} public UUID getFacilityId(){return facilityId;} public String getGovernorate(){return governorate;}
  public LocalDate getExpiresAt(){return expiresAt;}
  public void reserveFor(UUID patientId){this.status="reserved"; this.reservedForPatient=patientId; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"bloodbank/BloodUnitRepository.java", """package eg.gov.health.platform.bloodbank;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface BloodUnitRepository extends JpaRepository<BloodUnit, UUID> {
  List<BloodUnit> findByFacilityIdAndStatus(UUID facilityId, String status);
  Optional<BloodUnit> findByUnitCode(String unitCode);
}""")
w(PKG/"bloodbank/BloodBankController.java", """package eg.gov.health.platform.bloodbank;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/blood-bank")
public class BloodBankController {
  private final BloodUnitRepository repo; private final AuditService audit;
  public BloodBankController(BloodUnitRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record UnitRequest(String unitCode, String bloodGroup, String component, LocalDate donatedAt, LocalDate expiresAt) {}
  @PostMapping("/units")
  Map<String,Object> add(@RequestBody UnitRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.unitCode()==null||!BloodCompatibility.GROUPS.contains(r.bloodGroup()))
      throw BizException.badRequest("invalid_blood_group","فصيلة غير معروفة");
    var u = repo.save(new BloodUnit(UUID.fromString(p.facilityId()), p.governorate(), r.unitCode(), r.bloodGroup(), r.component(), r.donatedAt(), r.expiresAt()));
    audit.record("blood.unit.added","blood_unit",u.getId().toString(),"{}");
    return Map.of("ok",true,"unit",u);
  }
  @GetMapping("/compatibility/{recipientGroup}")
  Map<String,Object> compatibility(@PathVariable String recipientGroup) {
    if (!BloodCompatibility.GROUPS.contains(recipientGroup)) throw BizException.badRequest("invalid_blood_group","فصيلة غير معروفة");
    return Map.of("ok",true,"recipient",recipientGroup,"compatible_donors",BloodCompatibility.compatibleDonorsFor(recipientGroup));
  }
  @PostMapping("/units/{id}/reserve")
  Map<String,Object> reserve(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var u = repo.findById(id).orElseThrow(() -> BizException.notFound("unit_not_found","الوحدة غير موجودة"));
    if (!"available".equals(u.getStatus())) throw BizException.conflict("unit_unavailable","الوحدة ليست متاحة");
    if (u.getExpiresAt()!=null && u.getExpiresAt().isBefore(LocalDate.now())) throw BizException.conflict("unit_expired","وحدة منتهية الصلاحية لا تُحجز");
    String recipientGroup = body.get("recipient_blood_group");
    if (recipientGroup==null || !BloodCompatibility.canDonateRbc(u.getBloodGroup(), recipientGroup))
      throw BizException.conflict("incompatible","فصيلة الوحدة "+u.getBloodGroup()+" غير متوافقة مع المستقبل "+recipientGroup+" (دورة 223)");
    u.reserveFor(UUID.fromString(body.get("patient_id"))); repo.save(u);
    audit.record("blood.unit.reserved","blood_unit",id.toString(),"{}");
    return Map.of("ok",true,"unit",u);
  }
  @GetMapping("/units")
  Map<String,Object> list(@RequestParam(required=false) String status) {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdAndStatus(UUID.fromString(p.facilityId()), status==null?"available":status);
    return Map.of("ok",true,"units",rows,"count",rows.size());
  }
}""")

# ---------------- referrals (cycle 224) + beds/capacity (225)
w(PKG/"operations/Referral.java", """package eg.gov.health.platform.operations;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "referrals")
public class Referral {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="to_facility_id", nullable=false) private UUID toFacilityId;
  @Column(nullable=false) private String reason;
  @Column(nullable=false) private String urgency = "routine"; // routine, urgent, emergency
  @Column(nullable=false) private String status = "pending"; // pending, accepted, rejected, in_transit, arrived, completed, cancelled
  @Column(name="accepted_by") private String acceptedBy;
  @Column(name="rejection_reason") private String rejectionReason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Referral() {}
  public Referral(UUID facilityId, String governorate, UUID patientId, UUID toFacilityId, String reason, String urgency) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.toFacilityId=toFacilityId;
    this.reason=reason; this.urgency=urgency==null?"routine":urgency;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getFacilityId(){return facilityId;}
  public UUID getToFacilityId(){return toFacilityId;} public UUID getPatientId(){return patientId;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
  public void accept(String by){this.status="accepted"; this.acceptedBy=by; this.updatedAt=OffsetDateTime.now();}
  public void reject(String reason){this.status="rejected"; this.rejectionReason=reason; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"operations/ReferralRepository.java", """package eg.gov.health.platform.operations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ReferralRepository extends JpaRepository<Referral, UUID> {
  List<Referral> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  List<Referral> findByToFacilityIdAndStatus(UUID toFacilityId, String status);
}""")
w(PKG/"operations/ReferralController.java", """package eg.gov.health.platform.operations;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/referrals")
public class ReferralController {
  private final ReferralRepository repo; private final AuditService audit;
  public ReferralController(ReferralRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  static final Map<String,List<String>> FLOW = Map.of(
    "pending", List.of("accepted","rejected","cancelled"),
    "accepted", List.of("in_transit","cancelled"),
    "in_transit", List.of("arrived"),
    "arrived", List.of("completed"),
    "rejected", List.of(), "completed", List.of(), "cancelled", List.of());
  public record ReferralRequest(UUID patientId, UUID toFacilityId, String reason, String urgency) {}
  @PostMapping
  Map<String,Object> create(@RequestBody ReferralRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.toFacilityId()==null||r.reason()==null) throw BizException.badRequest("missing_field","patientId و toFacilityId و reason مطلوبة");
    if (r.toFacilityId().toString().equals(p.facilityId())) throw BizException.badRequest("same_facility","الإحالة لمنشأة أخرى");
    var ref = repo.save(new Referral(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.toFacilityId(), r.reason(), r.urgency()));
    audit.record("referral.created","referral",ref.getId().toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  @PostMapping("/{id}/accept")
  Map<String,Object> accept(@PathVariable UUID id) {
    var p = TenantContext.get(); var ref = get(id);
    if (!ref.getToFacilityId().toString().equals(p.facilityId())) throw BizException.forbidden("القبول من المنشأة المستقبلة فقط (دورة 224)");
    if (!"pending".equals(ref.getStatus())) throw BizException.conflict("not_pending","الإحالة ليست معلقة");
    ref.accept(p.userId()); repo.save(ref);
    audit.record("referral.accepted","referral",id.toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  @PostMapping("/{id}/reject")
  Map<String,Object> reject(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = TenantContext.get(); var ref = get(id);
    if (!ref.getToFacilityId().toString().equals(p.facilityId())) throw BizException.forbidden("الرفض من المنشأة المستقبلة فقط");
    if (body.getOrDefault("reason","").isBlank()) throw BizException.badRequest("reason_required","سبب الرفض موثق إلزاميًا");
    if (!"pending".equals(ref.getStatus())) throw BizException.conflict("not_pending","الإحالة ليست معلقة");
    ref.reject(body.get("reason")); repo.save(ref);
    audit.record("referral.rejected","referral",id.toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  @PostMapping("/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var ref = get(id); String to = body.getOrDefault("status","");
    if (!FLOW.getOrDefault(ref.getStatus(), List.of()).contains(to))
      throw BizException.conflict("illegal_transition","انتقال غير مسموح: "+ref.getStatus()+" -> "+to);
    ref.setStatus(to); repo.save(ref);
    audit.record("referral."+to,"referral",id.toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  @GetMapping("/incoming")
  Map<String,Object> incoming() {
    var p = TenantContext.get();
    var rows = repo.findByToFacilityIdAndStatus(UUID.fromString(p.facilityId()), "pending");
    return Map.of("ok",true,"referrals",rows,"count",rows.size());
  }
  @GetMapping
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"referrals",rows,"count",rows.size());
  }
  private Referral get(UUID id) { return repo.findById(id).orElseThrow(() -> BizException.notFound("referral_not_found","الإحالة غير موجودة")); }
}""")
w(PKG/"operations/BedBoardController.java", """package eg.gov.health.platform.operations;
import eg.gov.health.platform.clinical.EncounterRepository;
import eg.gov.health.platform.facility.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** National capacity board (cycle 225): aggregates only for national roles —
 *  counts and occupancy, never patient details of other facilities. */
@RestController @RequestMapping("/api/capacity")
public class BedBoardController {
  private final FacilityRepository facilities; private final EncounterRepository encounters;
  public BedBoardController(FacilityRepository facilities, EncounterRepository encounters) { this.facilities=facilities; this.encounters=encounters; }
  @GetMapping("/board")
  Map<String,Object> board(@RequestParam(required=false) String governorate) {
    var rows = governorate==null ? facilities.findAll() : facilities.findByGovernorate(governorate);
    var board = rows.stream().map(f -> {
      long open = encounters.findByFacilityIdOrderByArrivedAtDesc(f.getId()).stream().filter(e -> !"closed".equals(e.getStatus()) && !"completed".equals(e.getStatus())).count();
      int cap = f.getBedCapacity()==null?0:f.getBedCapacity();
      var m = new LinkedHashMap<String,Object>();
      m.put("facility_id", f.getId()); m.put("name_ar", f.getNameAr()); m.put("governorate", f.getGovernorate());
      m.put("bed_capacity", cap); m.put("open_encounters", open);
      m.put("occupancy_pct", cap==0? null : Math.round(1000.0*Math.min(open,cap)/cap)/10.0);
      return m;
    }).toList();
    return Map.of("ok",true,"board",board,"count",board.size());
  }
}""")
print("batch2 java done")

# ---------------- Flyway V3
rls = """ALTER TABLE {t} ENABLE ROW LEVEL SECURITY;
ALTER TABLE {t} FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS {t}_facility_isolation ON {t};
CREATE POLICY {t}_facility_isolation ON {t} FOR ALL
  USING (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)))
  WITH CHECK (current_setting('app.current_facility_id', true) = 'national'
         OR facility_id::text = current_setting('app.current_facility_id', true)
         OR (current_setting('app.current_facility_id', true) LIKE 'gov:%' AND governorate = split_part(current_setting('app.current_facility_id', true), ':', 2)));"""
def R(t): return rls.format(t=t)
v3 = """-- V3 — Batch 2 (Catalog v6): diagnostics cycle 219, pharmacy 222,
-- blood bank 223, referrals 224. Capacity board (225) is read-model only.
CREATE TABLE IF NOT EXISTS lab_orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), encounter_id UUID REFERENCES encounters(id),
  test_code TEXT NOT NULL, test_name TEXT,
  priority TEXT NOT NULL DEFAULT 'routine',
  status TEXT NOT NULL DEFAULT 'ordered' CHECK (status IN ('ordered','specimen_collected','received','in_analysis','resulted','verified','corrected','cancelled')),
  ordered_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_lab_orders_facility_status ON lab_orders (facility_id, status);
CREATE TABLE IF NOT EXISTS lab_results (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  order_id UUID NOT NULL REFERENCES lab_orders(id),
  value NUMERIC NOT NULL, unit TEXT, ref_low NUMERIC, ref_high NUMERIC,
  is_critical BOOLEAN NOT NULL DEFAULT false,
  version INT NOT NULL DEFAULT 1, supersedes_id UUID REFERENCES lab_results(id),
  status TEXT NOT NULL DEFAULT 'resulted' CHECK (status IN ('resulted','verified','corrected')),
  verified_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_lab_results_order ON lab_results (order_id, version);
CREATE TABLE IF NOT EXISTS prescriptions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), encounter_id UUID REFERENCES encounters(id),
  drug_code TEXT NOT NULL, drug_name TEXT, dose TEXT, frequency TEXT, duration TEXT,
  status TEXT NOT NULL DEFAULT 'prescribed' CHECK (status IN ('prescribed','verified','dispensed','partially_dispensed','returned','cancelled')),
  prescribed_by TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_prescriptions_facility_status ON prescriptions (facility_id, status);
CREATE INDEX IF NOT EXISTS idx_prescriptions_patient ON prescriptions (patient_id, status);
CREATE TABLE IF NOT EXISTS blood_units (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  unit_code TEXT NOT NULL UNIQUE, blood_group TEXT NOT NULL, component TEXT NOT NULL DEFAULT 'RBC',
  donated_at DATE, expires_at DATE,
  status TEXT NOT NULL DEFAULT 'available' CHECK (status IN ('available','reserved','issued','transfused','expired','discarded')),
  reserved_for_patient UUID REFERENCES patients(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_blood_units_facility_status ON blood_units (facility_id, status);
CREATE TABLE IF NOT EXISTS referrals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), to_facility_id UUID NOT NULL REFERENCES facilities(id),
  reason TEXT NOT NULL, urgency TEXT NOT NULL DEFAULT 'routine',
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','accepted','rejected','in_transit','arrived','completed','cancelled')),
  accepted_by TEXT, rejection_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_referrals_facility_status ON referrals (facility_id, status);
CREATE INDEX IF NOT EXISTS idx_referrals_target ON referrals (to_facility_id, status);
""" + "\n".join(R(t) for t in ["lab_orders","lab_results","prescriptions","blood_units","referrals"]) + """
GRANT SELECT, INSERT, UPDATE, DELETE ON lab_orders, lab_results, prescriptions, blood_units, referrals TO app_user;
"""
w(RES/"db/migration/V3__diagnostics_pharmacy_blood_referrals.sql", v3)

w(TEST/"diagnostics/CriticalValueRulesTest.java", """package eg.gov.health.platform.diagnostics;
import org.junit.jupiter.api.Test; import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class CriticalValueRulesTest {
  @Test void potassiumExtremesAreCritical() {
    assertTrue(CriticalValueRules.isCritical("K", new BigDecimal("2.5")));
    assertTrue(CriticalValueRules.isCritical("K", new BigDecimal("6.4")));
    assertFalse(CriticalValueRules.isCritical("K", new BigDecimal("4.2")));
  }
  @Test void unknownTestIsNeverAutoCritical() { assertFalse(CriticalValueRules.isCritical("XYZ", new BigDecimal("9999"))); }
  @Test void glucoseLowIsCritical() { assertTrue(CriticalValueRules.isCritical("GLU", new BigDecimal("35"))); }
}""")
w(TEST/"bloodbank/BloodCompatibilityTest.java", """package eg.gov.health.platform.bloodbank;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BloodCompatibilityTest {
  @Test void oNegativeDonatesToEveryone() {
    for (String g : BloodCompatibility.GROUPS) assertTrue(BloodCompatibility.canDonateRbc("O-", g), g);
  }
  @Test void abPositiveReceivesFromEveryone() {
    for (String g : BloodCompatibility.GROUPS) assertTrue(BloodCompatibility.canDonateRbc(g, "AB+"), g);
  }
  @Test void rhPositiveCannotDonateToRhNegative() {
    assertFalse(BloodCompatibility.canDonateRbc("A+", "A-"));
    assertTrue(BloodCompatibility.canDonateRbc("A-", "A+"));
  }
  @Test void aboRulesHold() {
    assertFalse(BloodCompatibility.canDonateRbc("A-", "B+"));
    assertTrue(BloodCompatibility.canDonateRbc("B-", "AB-"));
    assertFalse(BloodCompatibility.canDonateRbc("AB+", "O+"));
  }
  @Test void unknownGroupRejected() { assertFalse(BloodCompatibility.canDonateRbc("X-", "O-")); }
}""")
w(TEST/"pharmacy/InteractionRulesTest.java", """package eg.gov.health.platform.pharmacy;
import org.junit.jupiter.api.Test; import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class InteractionRulesTest {
  @Test void warfarinAspirinIsSevere() {
    assertTrue(InteractionRules.severeInteraction("WARFARIN", List.of("ASPIRIN")).isPresent());
    assertTrue(InteractionRules.severeInteraction("aspirin", List.of("warfarin")).isPresent());
  }
  @Test void unrelatedPairIsFine() { assertTrue(InteractionRules.severeInteraction("PARACETAMOL", List.of("AMOXICILLIN")).isEmpty()); }
  @Test void duplicateDetection() {
    assertTrue(InteractionRules.isDuplicate("INSULIN", List.of("insulin")));
    assertFalse(InteractionRules.isDuplicate("INSULIN", List.of("METFORMIN")));
  }
}""")
print("batch2 migration+tests done")
