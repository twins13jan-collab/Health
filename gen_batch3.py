#!/usr/bin/env python3
"""Batch 3: consent & break-glass (218), billing & claims (228),
inventory, appointments, vaccinations — Java modules + Flyway V4."""
import pathlib
BASE = pathlib.Path.home() / "workspace/egypt-health-v6/services/platform-api"
PKG = BASE / "src/main/java/eg/gov/health/platform"
RES = BASE / "src/main/resources"
TEST = BASE / "src/test/java/eg/gov/health/platform"
def w(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.strip() + "\n", encoding="utf-8"); print("wrote", path.name)

# ---------------- consent (cycle 218) + break-glass
w(PKG/"governance/ConsentRules.java", """package eg.gov.health.platform.governance;
import java.time.OffsetDateTime;
/** Consent validity (cycle 218): a share is allowed ONLY when an active
 *  consent exists for the same purpose + recipient + data scope, inside its
 *  validity window, and not revoked. Revocation stops FUTURE use; it never
 *  erases processing that had a lawful basis when it happened. */
public final class ConsentRules {
  private ConsentRules() {}
  public record ConsentView(String purpose, String recipient, String dataScope, String status,
                            OffsetDateTime effectiveFrom, OffsetDateTime expiresAt) {}
  public static boolean isShareAllowed(ConsentView c, String purpose, String recipient, String dataScope, OffsetDateTime now) {
    if (c == null || !"active".equals(c.status())) return false;
    if (!purpose.equals(c.purpose()) || !recipient.equals(c.recipient()) || !dataScope.equals(c.dataScope())) return false;
    if (c.effectiveFrom() != null && now.isBefore(c.effectiveFrom())) return false;
    return c.expiresAt() == null || now.isBefore(c.expiresAt());
  }
  public static boolean isBreakGlassJustifiable(String reason, int requestedMinutes) {
    return reason != null && reason.trim().length() >= 10 && requestedMinutes > 0 && requestedMinutes <= 240;
  }
}""")
w(PKG/"governance/ConsentRecord.java", """package eg.gov.health.platform.governance;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "consent_records")
public class ConsentRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String purpose;   // treatment, disclosure, research, communication, imaging, secondary_use
  @Column(nullable=false) private String recipient;
  @Column(name="data_scope", nullable=false) private String dataScope;
  @Column(name="consent_version") private String consentVersion;
  @Column(nullable=false) private String status = "active"; // active, revoked, expired
  @Column(name="effective_from") private OffsetDateTime effectiveFrom = OffsetDateTime.now();
  @Column(name="expires_at") private OffsetDateTime expiresAt;
  @Column(name="revoked_at") private OffsetDateTime revokedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ConsentRecord() {}
  public ConsentRecord(UUID facilityId, String governorate, UUID patientId, String purpose, String recipient, String dataScope, String consentVersion, OffsetDateTime expiresAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.purpose=purpose;
    this.recipient=recipient; this.dataScope=dataScope; this.consentVersion=consentVersion; this.expiresAt=expiresAt;
  }
  public UUID getId(){return id;} public UUID getPatientId(){return patientId;} public String getPurpose(){return purpose;}
  public String getRecipient(){return recipient;} public String getDataScope(){return dataScope;} public String getStatus(){return status;}
  public OffsetDateTime getEffectiveFrom(){return effectiveFrom;} public OffsetDateTime getExpiresAt(){return expiresAt;}
  public void revoke(){this.status="revoked"; this.revokedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"governance/ConsentRecordRepository.java", """package eg.gov.health.platform.governance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {
  List<ConsentRecord> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
  List<ConsentRecord> findByPatientIdAndPurposeAndRecipientAndDataScopeAndStatus(UUID patientId, String purpose, String recipient, String dataScope, String status);
}""")
w(PKG/"governance/BreakGlassAccess.java", """package eg.gov.health.platform.governance;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "break_glass_access")
public class BreakGlassAccess {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="actor_id", nullable=false) private String actorId;
  @Column(nullable=false) private String reason;
  @Column(name="duration_minutes", nullable=false) private int durationMinutes;
  @Column(nullable=false) private String status = "active"; // active, expired, reviewed, flagged
  @Column(name="reviewed_by") private String reviewedBy;
  @Column(name="review_outcome") private String reviewOutcome;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected BreakGlassAccess() {}
  public BreakGlassAccess(UUID facilityId, String governorate, UUID patientId, String actorId, String reason, int durationMinutes) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.actorId=actorId;
    this.reason=reason; this.durationMinutes=durationMinutes;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getPatientId(){return patientId;}
  public void review(String by, String outcome){this.reviewedBy=by; this.reviewOutcome=outcome; this.status="flagged".equals(outcome)?"flagged":"reviewed"; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"governance/BreakGlassAccessRepository.java", """package eg.gov.health.platform.governance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface BreakGlassAccessRepository extends JpaRepository<BreakGlassAccess, UUID> {
  List<BreakGlassAccess> findByStatusOrderByCreatedAtAsc(String status);
}""")
w(PKG/"governance/ConsentController.java", """package eg.gov.health.platform.governance;
import eg.gov.health.platform.common.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/consents")
public class ConsentController {
  private final ConsentRecordRepository consents; private final BreakGlassAccessRepository breakGlass; private final AuditService audit;
  public ConsentController(ConsentRecordRepository consents, BreakGlassAccessRepository breakGlass, AuditService audit) {
    this.consents=consents; this.breakGlass=breakGlass; this.audit=audit;
  }
  public record ConsentRequest(UUID patientId, String purpose, String recipient, String dataScope, String consentVersion, OffsetDateTime expiresAt) {}
  @PostMapping
  Map<String,Object> grant(@RequestBody ConsentRequest r) {
    var p = TenantContext.get();
    if (r.patientId()==null||r.purpose()==null||r.recipient()==null||r.dataScope()==null)
      throw BizException.badRequest("missing_field","patientId و purpose و recipient و dataScope مطلوبة — الموافقة بلا نطاق لا قيمة لها");
    var c = consents.save(new ConsentRecord(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.purpose(), r.recipient(), r.dataScope(), r.consentVersion(), r.expiresAt()));
    audit.record("consent.granted","consent",c.getId().toString(),"{}");
    return Map.of("ok",true,"consent",c);
  }
  @PostMapping("/{id}/revoke")
  Map<String,Object> revoke(@PathVariable UUID id) {
    var c = consents.findById(id).orElseThrow(() -> BizException.notFound("consent_not_found","الموافقة غير موجودة"));
    c.revoke(); consents.save(c);
    audit.record("consent.revoked","consent",id.toString(),"{}");
    return Map.of("ok",true,"consent",c,"note","السحب يوقف الاستخدامات المستقبلية ولا يمحو معالجة سابقة قامت على أساس قانوني (دورة 218)");
  }
  @GetMapping("/check")
  Map<String,Object> check(@RequestParam UUID patientId, @RequestParam String purpose, @RequestParam String recipient, @RequestParam String dataScope) {
    var now = OffsetDateTime.now();
    boolean allowed = consents.findByPatientIdAndPurposeAndRecipientAndDataScopeAndStatus(patientId, purpose, recipient, dataScope, "active")
      .stream().anyMatch(c -> ConsentRules.isShareAllowed(
        new ConsentRules.ConsentView(c.getPurpose(), c.getRecipient(), c.getDataScope(), c.getStatus(), c.getEffectiveFrom(), c.getExpiresAt()),
        purpose, recipient, dataScope, now));
    audit.record(allowed?"consent.share.allowed":"consent.share.denied","patient",patientId.toString(),"{}");
    return Map.of("ok",true,"share_allowed",allowed);
  }
  @GetMapping("/patient/{patientId}")
  Map<String,Object> byPatient(@PathVariable UUID patientId) {
    var rows = consents.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"consents",rows,"count",rows.size());
  }
  public record BreakGlassRequest(UUID patientId, String reason, Integer durationMinutes) {}
  @PostMapping("/break-glass")
  Map<String,Object> breakGlass(@RequestBody BreakGlassRequest r) {
    var p = TenantContext.get();
    int mins = r.durationMinutes()==null?60:r.durationMinutes();
    if (r.patientId()==null || !ConsentRules.isBreakGlassJustifiable(r.reason(), mins))
      throw BizException.badRequest("unjustified_break_glass","الوصول الاستثنائي يتطلب سببًا موثقًا (10 أحرف فأكثر) ومدة لا تتجاوز 240 دقيقة");
    var bg = breakGlass.save(new BreakGlassAccess(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), p==null?null:p.userId(), r.reason(), mins));
    audit.record("consent.break_glass.opened","break_glass",bg.getId().toString(),"{}");
    return Map.of("ok",true,"break_glass",bg,"note","يخضع لمراجعة لاحقة إلزامية من مسؤول الخصوصية");
  }
  @GetMapping("/break-glass/pending-review") @PreAuthorize("hasAnyRole('national_admin','governorate_admin')")
  Map<String,Object> pendingReview() {
    var rows = breakGlass.findByStatusOrderByCreatedAtAsc("active");
    return Map.of("ok",true,"pending_review",rows,"count",rows.size());
  }
  @PostMapping("/break-glass/{id}/review") @PreAuthorize("hasAnyRole('national_admin','governorate_admin')")
  Map<String,Object> review(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = TenantContext.get();
    var bg = breakGlass.findById(id).orElseThrow(() -> BizException.notFound("break_glass_not_found","غير موجود"));
    bg.review(p==null?null:p.userId(), body.getOrDefault("outcome","reviewed")); breakGlass.save(bg);
    audit.record("consent.break_glass.reviewed","break_glass",id.toString(),"{}");
    return Map.of("ok",true,"break_glass",bg);
  }
}""")

# ---------------- billing & claims (cycle 228)
w(PKG/"finance/BillingCalculator.java", """package eg.gov.health.platform.finance;
import java.math.BigDecimal; import java.math.RoundingMode; import java.util.List;
/** Invoice mathematics (cycle 228): totals are computed server-side only,
 *  coverage splits patient/insurer shares, and a claim can only be priced
 *  from verified line items — never from a client-supplied total. */
public final class BillingCalculator {
  private BillingCalculator() {}
  public record Line(BigDecimal unitPrice, int quantity, BigDecimal coveragePct) {}
  public static BigDecimal lineTotal(Line l) { return l.unitPrice().multiply(BigDecimal.valueOf(l.quantity())).setScale(2, RoundingMode.HALF_UP); }
  public static BigDecimal grandTotal(List<Line> lines) { return lines.stream().map(BillingCalculator::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
  public static BigDecimal insurerShare(List<Line> lines) {
    return lines.stream().map(l -> lineTotal(l).multiply(l.coveragePct()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP))
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
  public static BigDecimal patientShare(List<Line> lines) { return grandTotal(lines).subtract(insurerShare(lines)); }
}""")
w(PKG/"finance/Invoice.java", """package eg.gov.health.platform.finance;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "invoices")
public class Invoice {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="encounter_id") private UUID encounterId;
  @Column(name="total_amount", nullable=false) private BigDecimal totalAmount = BigDecimal.ZERO;
  @Column(name="insurer_amount", nullable=false) private BigDecimal insurerAmount = BigDecimal.ZERO;
  @Column(name="patient_amount", nullable=false) private BigDecimal patientAmount = BigDecimal.ZERO;
  @Column(nullable=false) private String status = "draft"; // draft, issued, claimed, partially_paid, paid, rejected, cancelled
  @Column(name="payer") private String payer;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Invoice() {}
  public Invoice(UUID facilityId, String governorate, UUID patientId, UUID encounterId, String payer) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.encounterId=encounterId; this.payer=payer;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public BigDecimal getTotalAmount(){return totalAmount;}
  public BigDecimal getInsurerAmount(){return insurerAmount;} public BigDecimal getPatientAmount(){return patientAmount;}
  public UUID getFacilityId(){return facilityId;} public UUID getPatientId(){return patientId;}
  public void setTotals(BigDecimal t, BigDecimal i, BigDecimal p){this.totalAmount=t; this.insurerAmount=i; this.patientAmount=p; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}""")
w(PKG/"finance/InvoiceLine.java", """package eg.gov.health.platform.finance;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "invoice_lines")
public class InvoiceLine {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="invoice_id", nullable=false) private UUID invoiceId;
  @Column(nullable=false) private String description;
  @Column(name="unit_price", nullable=false) private BigDecimal unitPrice;
  @Column(nullable=false) private int quantity = 1;
  @Column(name="coverage_pct", nullable=false) private BigDecimal coveragePct = BigDecimal.ZERO;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected InvoiceLine() {}
  public InvoiceLine(UUID facilityId, String governorate, UUID invoiceId, String description, BigDecimal unitPrice, int quantity, BigDecimal coveragePct) {
    this.facilityId=facilityId; this.governorate=governorate; this.invoiceId=invoiceId; this.description=description;
    this.unitPrice=unitPrice; this.quantity=quantity; this.coveragePct=coveragePct==null?BigDecimal.ZERO:coveragePct;
  }
  public UUID getId(){return id;} public UUID getInvoiceId(){return invoiceId;}
  public BillingCalculator.Line toLine(){ return new BillingCalculator.Line(unitPrice, quantity, coveragePct); }
}""")
w(PKG/"finance/InvoiceRepository.java", """package eg.gov.health.platform.finance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
  List<Invoice> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
}""")
w(PKG/"finance/InvoiceLineRepository.java", """package eg.gov.health.platform.finance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, UUID> {
  List<InvoiceLine> findByInvoiceId(UUID invoiceId);
}""")
w(PKG/"finance/BillingController.java", """package eg.gov.health.platform.finance;
import eg.gov.health.platform.common.*;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.util.*;
@RestController @RequestMapping("/api/billing")
public class BillingController {
  private final InvoiceRepository invoices; private final InvoiceLineRepository lines; private final AuditService audit;
  public BillingController(InvoiceRepository invoices, InvoiceLineRepository lines, AuditService audit) { this.invoices=invoices; this.lines=lines; this.audit=audit; }
  static final Map<String,List<String>> FLOW = Map.of(
    "draft", List.of("issued","cancelled"), "issued", List.of("claimed","paid","cancelled"),
    "claimed", List.of("partially_paid","paid","rejected"), "partially_paid", List.of("paid"),
    "paid", List.of(), "rejected", List.of("issued"), "cancelled", List.of());
  public record InvoiceRequest(UUID patientId, UUID encounterId, String payer) {}
  @PostMapping("/invoices")
  Map<String,Object> create(@RequestBody InvoiceRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null) throw BizException.badRequest("missing_field","patientId مطلوب");
    var inv = invoices.save(new Invoice(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.encounterId(), r.payer()));
    audit.record("billing.invoice.created","invoice",inv.getId().toString(),"{}");
    return Map.of("ok",true,"invoice",inv);
  }
  public record LineRequest(String description, BigDecimal unitPrice, Integer quantity, BigDecimal coveragePct) {}
  @PostMapping("/invoices/{id}/lines") @Transactional
  Map<String,Object> addLine(@PathVariable UUID id, @RequestBody LineRequest r) {
    var p = TenantContext.get();
    var inv = invoices.findById(id).orElseThrow(() -> BizException.notFound("invoice_not_found","الفاتورة غير موجودة"));
    if (!"draft".equals(inv.getStatus())) throw BizException.conflict("invoice_locked","لا تُعدَّل فاتورة بعد إصدارها — أنشئ إشعارًا دائنًا/تصحيحًا (دورة 228)");
    if (r.description()==null||r.unitPrice()==null) throw BizException.badRequest("missing_field","description و unitPrice مطلوبان");
    lines.save(new InvoiceLine(inv.getFacilityId(), p==null?null:p.governorate(), id, r.description(), r.unitPrice(), r.quantity()==null?1:r.quantity(), r.coveragePct()));
    retotal(inv);
    return Map.of("ok",true,"invoice",inv,"lines",lines.findByInvoiceId(id));
  }
  private void retotal(Invoice inv) {
    var ls = lines.findByInvoiceId(inv.getId()).stream().map(InvoiceLine::toLine).toList();
    inv.setTotals(BillingCalculator.grandTotal(ls), BillingCalculator.insurerShare(ls), BillingCalculator.patientShare(ls));
    invoices.save(inv);
  }
  @PostMapping("/invoices/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var inv = invoices.findById(id).orElseThrow(() -> BizException.notFound("invoice_not_found","الفاتورة غير موجودة"));
    String to = body.getOrDefault("status","");
    if (!FLOW.getOrDefault(inv.getStatus(), List.of()).contains(to))
      throw BizException.conflict("illegal_transition","انتقال غير مسموح: "+inv.getStatus()+" -> "+to);
    inv.setStatus(to); invoices.save(inv);
    audit.record("billing.invoice."+to,"invoice",id.toString(),"{}");
    return Map.of("ok",true,"invoice",inv);
  }
  @GetMapping("/invoices")
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = invoices.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"invoices",rows,"count",rows.size());
  }
}""")

# ---------------- inventory
w(PKG/"operations/StockItem.java", """package eg.gov.health.platform.operations;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "stock_items")
public class StockItem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="item_code", nullable=false) private String itemCode;
  @Column(nullable=false) private String name;
  @Column(nullable=false) private int quantity = 0;
  @Column(name="reorder_level", nullable=false) private int reorderLevel = 0;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected StockItem() {}
  public StockItem(UUID facilityId, String governorate, String itemCode, String name, int reorderLevel) {
    this.facilityId=facilityId; this.governorate=governorate; this.itemCode=itemCode; this.name=name; this.reorderLevel=reorderLevel;
  }
  public UUID getId(){return id;} public int getQuantity(){return quantity;} public String getItemCode(){return itemCode;}
  public int getReorderLevel(){return reorderLevel;} public UUID getFacilityId(){return facilityId;}
  public boolean applyMovement(int delta) {
    if (quantity + delta < 0) return false; // stock can never go negative — the movement is rejected, not clamped
    quantity += delta; updatedAt = OffsetDateTime.now(); return true;
  }
}""")
w(PKG/"operations/StockItemRepository.java", """package eg.gov.health.platform.operations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface StockItemRepository extends JpaRepository<StockItem, UUID> {
  Optional<StockItem> findByFacilityIdAndItemCode(UUID facilityId, String itemCode);
  List<StockItem> findByFacilityIdOrderByItemCode(UUID facilityId);
}""")
w(PKG/"operations/InventoryController.java", """package eg.gov.health.platform.operations;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/inventory")
public class InventoryController {
  private final StockItemRepository repo; private final AuditService audit;
  public InventoryController(StockItemRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record ItemRequest(String itemCode, String name, Integer reorderLevel) {}
  @PostMapping("/items")
  Map<String,Object> create(@RequestBody ItemRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.itemCode()==null||r.name()==null) throw BizException.badRequest("missing_field","itemCode و name مطلوبان");
    var item = repo.save(new StockItem(UUID.fromString(p.facilityId()), p.governorate(), r.itemCode(), r.name(), r.reorderLevel()==null?0:r.reorderLevel()));
    return Map.of("ok",true,"item",item);
  }
  public record MovementRequest(Integer delta, String reason) {}
  @PostMapping("/items/{itemCode}/movements")
  Map<String,Object> movement(@PathVariable String itemCode, @RequestBody MovementRequest r) {
    var p = TenantContext.get();
    var item = repo.findByFacilityIdAndItemCode(UUID.fromString(p.facilityId()), itemCode)
      .orElseThrow(() -> BizException.notFound("item_not_found","الصنف غير موجود"));
    if (r.delta()==null||r.delta()==0) throw BizException.badRequest("invalid_delta","حركة صفرية غير مسموحة");
    if (r.reason()==null||r.reason().isBlank()) throw BizException.badRequest("reason_required","سبب الحركة موثق إلزاميًا");
    if (!item.applyMovement(r.delta())) throw BizException.conflict("insufficient_stock","المخزون لا يكفي — تُرفض الحركة ولا يُقصّ الرصيد (مصالحة مخزون)");
    repo.save(item);
    audit.record(r.delta()>0?"inventory.stock_in":"inventory.stock_out","stock_item",item.getId().toString(),"{}");
    var out = new HashMap<String,Object>(); out.put("ok",true); out.put("item",item);
    if (item.getQuantity() <= item.getReorderLevel()) out.put("alert","الرصيد عند أو تحت حد إعادة الطلب");
    return out;
  }
  @GetMapping("/items")
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByItemCode(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"items",rows,"count",rows.size());
  }
}""")

# ---------------- appointments + vaccinations
w(PKG/"clinical/Appointment.java", """package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "appointments")
public class Appointment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="clinic") private String clinic;
  @Column(name="doctor_name") private String doctorName;
  @Column(name="starts_at", nullable=false) private OffsetDateTime startsAt;
  @Column(name="ends_at", nullable=false) private OffsetDateTime endsAt;
  @Column(nullable=false) private String status = "booked"; // booked, confirmed, attended, no_show, cancelled, rescheduled
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Appointment() {}
  public Appointment(UUID facilityId, String governorate, UUID patientId, String clinic, String doctorName, OffsetDateTime startsAt, OffsetDateTime endsAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.clinic=clinic;
    this.doctorName=doctorName; this.startsAt=startsAt; this.endsAt=endsAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public OffsetDateTime getStartsAt(){return startsAt;} public OffsetDateTime getEndsAt(){return endsAt;}
  public String getDoctorName(){return doctorName;} public UUID getFacilityId(){return facilityId;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
  public boolean overlaps(Appointment o) {
    return startsAt.isBefore(o.endsAt) && o.startsAt.isBefore(endsAt) && !"cancelled".equals(o.status);
  }
}""")
w(PKG/"clinical/AppointmentRepository.java", """package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
  List<Appointment> findByFacilityIdAndDoctorName(UUID facilityId, String doctorName);
  List<Appointment> findByPatientIdOrderByStartsAtAsc(UUID patientId);
}""")
w(PKG/"clinical/AppointmentController.java", """package eg.gov.health.platform.clinical;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/appointments")
public class AppointmentController {
  private final AppointmentRepository repo; private final AuditService audit;
  public AppointmentController(AppointmentRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record ApptRequest(UUID patientId, String clinic, String doctorName, OffsetDateTime startsAt, OffsetDateTime endsAt) {}
  @PostMapping
  Map<String,Object> book(@RequestBody ApptRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.startsAt()==null||r.endsAt()==null||!r.endsAt().isAfter(r.startsAt()))
      throw BizException.badRequest("invalid_slot","فترة موعد غير صحيحة");
    var candidate = new Appointment(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.clinic(), r.doctorName(), r.startsAt(), r.endsAt());
    if (r.doctorName()!=null) {
      boolean clash = repo.findByFacilityIdAndDoctorName(candidate.getFacilityId(), r.doctorName()).stream().anyMatch(candidate::overlaps);
      if (clash) throw BizException.conflict("slot_conflict","تعارض مع موعد قائم لنفس الطبيب — لا حجز مزدوج");
    }
    var saved = repo.save(candidate);
    audit.record("appointment.booked","appointment",saved.getId().toString(),"{}");
    return Map.of("ok",true,"appointment",saved);
  }
  @PostMapping("/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var a = repo.findById(id).orElseThrow(() -> BizException.notFound("appointment_not_found","الموعد غير موجود"));
    String to = body.getOrDefault("status","");
    if (!List.of("booked","confirmed","attended","no_show","cancelled","rescheduled").contains(to))
      throw BizException.badRequest("invalid_status","حالة غير معروفة");
    a.setStatus(to); repo.save(a);
    audit.record("appointment."+to,"appointment",id.toString(),"{}");
    return Map.of("ok",true,"appointment",a);
  }
  @GetMapping
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = repo.findByPatientIdOrderByStartsAtAsc(patientId);
    return Map.of("ok",true,"appointments",rows,"count",rows.size());
  }
}""")
w(PKG/"clinical/VaccinationSchedule.java", """package eg.gov.health.platform.clinical;
import java.time.LocalDate; import java.util.*;
/** Simplified Egyptian EPI schedule checkpoints (module 96/102): the due
 *  logic is age-based and deterministic; the full national schedule table is
 *  data (vaccination_schedule table) — this class holds the core milestones
 *  used for due/overdue computation and catch-up flagging. */
public final class VaccinationSchedule {
  private VaccinationSchedule() {}
  public record Milestone(String vaccine, int dueAgeDays) {}
  public static final List<Milestone> MILESTONES = List.of(
    new Milestone("BCG", 0), new Milestone("OPV0", 0), new Milestone("HBV1", 0),
    new Milestone("PENTA1", 60), new Milestone("PENTA2", 120), new Milestone("PENTA3", 180),
    new Milestone("MMR1", 365), new Milestone("MMR2", 545));
  public static List<Milestone> dueMilestones(LocalDate birthDate, LocalDate onDate, Set<String> givenVaccines) {
    long age = java.time.temporal.ChronoUnit.DAYS.between(birthDate, onDate);
    return MILESTONES.stream().filter(m -> m.dueAgeDays() <= age && !givenVaccines.contains(m.vaccine())).toList();
  }
}""")
w(PKG/"clinical/Vaccination.java", """package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "vaccinations")
public class Vaccination {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(nullable=false) private String vaccine;
  @Column(name="dose_number") private Integer doseNumber;
  @Column(name="given_at", nullable=false) private LocalDate givenAt;
  @Column(name="batch_number") private String batchNumber;
  @Column(nullable=false) private String status = "given"; // given, refused, contraindicated
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Vaccination() {}
  public Vaccination(UUID facilityId, String governorate, UUID patientId, String vaccine, Integer doseNumber, LocalDate givenAt, String batchNumber) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.vaccine=vaccine;
    this.doseNumber=doseNumber; this.givenAt=givenAt; this.batchNumber=batchNumber;
  }
  public UUID getId(){return id;} public String getVaccine(){return vaccine;} public UUID getPatientId(){return patientId;}
}""")
w(PKG/"clinical/VaccinationRepository.java", """package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface VaccinationRepository extends JpaRepository<Vaccination, UUID> {
  List<Vaccination> findByPatientId(UUID patientId);
}""")
w(PKG/"clinical/VaccinationController.java", """package eg.gov.health.platform.clinical;
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
}""")
print("batch3 java done")

# ---------------- Flyway V4
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
v4 = """-- V4 — Batch 3 (Catalog v6): consent 218, billing/claims 228,
-- inventory, appointments, vaccinations.
CREATE TABLE IF NOT EXISTS consent_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  purpose TEXT NOT NULL, recipient TEXT NOT NULL, data_scope TEXT NOT NULL,
  consent_version TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','revoked','expired')),
  effective_from TIMESTAMPTZ NOT NULL DEFAULT now(), expires_at TIMESTAMPTZ, revoked_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_consents_patient ON consent_records (patient_id, status);
CREATE TABLE IF NOT EXISTS break_glass_access (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  actor_id TEXT NOT NULL, reason TEXT NOT NULL, duration_minutes INT NOT NULL,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','expired','reviewed','flagged')),
  reviewed_by TEXT, review_outcome TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS invoices (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id), encounter_id UUID REFERENCES encounters(id),
  total_amount NUMERIC NOT NULL DEFAULT 0, insurer_amount NUMERIC NOT NULL DEFAULT 0, patient_amount NUMERIC NOT NULL DEFAULT 0,
  status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft','issued','claimed','partially_paid','paid','rejected','cancelled')),
  payer TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_invoices_facility_status ON invoices (facility_id, status);
CREATE TABLE IF NOT EXISTS invoice_lines (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  invoice_id UUID NOT NULL REFERENCES invoices(id),
  description TEXT NOT NULL, unit_price NUMERIC NOT NULL, quantity INT NOT NULL DEFAULT 1,
  coverage_pct NUMERIC NOT NULL DEFAULT 0, status TEXT NOT NULL DEFAULT 'active',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_invoice_lines_invoice ON invoice_lines (invoice_id);
CREATE TABLE IF NOT EXISTS stock_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  item_code TEXT NOT NULL, name TEXT NOT NULL, quantity INT NOT NULL DEFAULT 0,
  reorder_level INT NOT NULL DEFAULT 0, status TEXT NOT NULL DEFAULT 'active',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_stock_facility_code UNIQUE (facility_id, item_code),
  CONSTRAINT chk_stock_nonnegative CHECK (quantity >= 0));
CREATE TABLE IF NOT EXISTS appointments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID NOT NULL REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  clinic TEXT, doctor_name TEXT, starts_at TIMESTAMPTZ NOT NULL, ends_at TIMESTAMPTZ NOT NULL,
  status TEXT NOT NULL DEFAULT 'booked' CHECK (status IN ('booked','confirmed','attended','no_show','cancelled','rescheduled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_appt_window CHECK (ends_at > starts_at));
CREATE INDEX IF NOT EXISTS idx_appointments_facility_doctor ON appointments (facility_id, doctor_name, starts_at);
CREATE TABLE IF NOT EXISTS vaccinations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facility_id UUID REFERENCES facilities(id), governorate TEXT,
  patient_id UUID NOT NULL REFERENCES patients(id),
  vaccine TEXT NOT NULL, dose_number INT, given_at DATE NOT NULL, batch_number TEXT,
  status TEXT NOT NULL DEFAULT 'given',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX IF NOT EXISTS idx_vaccinations_patient ON vaccinations (patient_id);
""" + "\n".join(rls_fac.format(t=t) for t in ["invoices","invoice_lines","stock_items","appointments"]) + """
GRANT SELECT, INSERT, UPDATE, DELETE ON consent_records, break_glass_access, invoices, invoice_lines, stock_items, appointments, vaccinations TO app_user;
"""
w(RES/"db/migration/V4__consent_billing_inventory_appointments_vaccinations.sql", v4)

w(TEST/"governance/ConsentRulesTest.java", """package eg.gov.health.platform.governance;
import org.junit.jupiter.api.Test; import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;
class ConsentRulesTest {
  ConsentRules.ConsentView active() { return new ConsentRules.ConsentView("disclosure","insurer-x","summary","active", OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(30)); }
  @Test void exactScopeAllowed() { assertTrue(ConsentRules.isShareAllowed(active(), "disclosure","insurer-x","summary", OffsetDateTime.now())); }
  @Test void differentPurposeDenied() { assertFalse(ConsentRules.isShareAllowed(active(), "research","insurer-x","summary", OffsetDateTime.now())); }
  @Test void revokedDenied() {
    var c = new ConsentRules.ConsentView("disclosure","insurer-x","summary","revoked", OffsetDateTime.now().minusDays(1), null);
    assertFalse(ConsentRules.isShareAllowed(c, "disclosure","insurer-x","summary", OffsetDateTime.now()));
  }
  @Test void expiredDenied() {
    var c = new ConsentRules.ConsentView("disclosure","insurer-x","summary","active", OffsetDateTime.now().minusDays(10), OffsetDateTime.now().minusDays(1));
    assertFalse(ConsentRules.isShareAllowed(c, "disclosure","insurer-x","summary", OffsetDateTime.now()));
  }
  @Test void breakGlassNeedsReasonAndBoundedDuration() {
    assertTrue(ConsentRules.isBreakGlassJustifiable("unconscious patient, allergy unknown", 60));
    assertFalse(ConsentRules.isBreakGlassJustifiable("urgent", 60));
    assertFalse(ConsentRules.isBreakGlassJustifiable("unconscious patient, allergy unknown", 999));
  }
}""")
w(TEST/"finance/BillingCalculatorTest.java", """package eg.gov.health.platform.finance;
import org.junit.jupiter.api.Test; import java.math.BigDecimal; import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class BillingCalculatorTest {
  @Test void totalsAndSplit() {
    var lines = List.of(
      new BillingCalculator.Line(new BigDecimal("100.00"), 2, new BigDecimal("80")),
      new BillingCalculator.Line(new BigDecimal("50.00"), 1, BigDecimal.ZERO));
    assertEquals(new BigDecimal("250.00"), BillingCalculator.grandTotal(lines));
    assertEquals(new BigDecimal("160.00"), BillingCalculator.insurerShare(lines));
    assertEquals(new BigDecimal("90.00"), BillingCalculator.patientShare(lines));
  }
  @Test void emptyInvoiceIsZero() { assertEquals(BigDecimal.ZERO, BillingCalculator.grandTotal(List.of())); }
}""")
w(TEST/"clinical/VaccinationScheduleTest.java", """package eg.gov.health.platform.clinical;
import org.junit.jupiter.api.Test; import java.time.LocalDate; import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class VaccinationScheduleTest {
  @Test void newbornDueBirthDosesOnly() {
    var due = VaccinationSchedule.dueMilestones(LocalDate.of(2026,1,1), LocalDate.of(2026,1,2), Set.of());
    assertEquals(3, due.size());
  }
  @Test void givenDosesExcludedAndLaterOnesAppear() {
    var due = VaccinationSchedule.dueMilestones(LocalDate.of(2025,1,1), LocalDate.of(2025,4,1), Set.of("BCG","OPV0","HBV1"));
    assertTrue(due.stream().anyMatch(m -> m.vaccine().equals("PENTA1")));
    assertFalse(due.stream().anyMatch(m -> m.vaccine().equals("BCG")));
  }
}""")
print("batch3 migration+tests done")
