package eg.gov.health.platform.finance;
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
  /** Finance operations board: the facility's receivables as an
   *  attention queue — rejected first, stale drafts, then aging
   *  receivables — with aging buckets and outstanding totals
   *  split by insurer and patient share. Scope follows the
   *  caller's tier exactly like the invoice list. */
  @GetMapping("/board")
  Map<String,Object> board() {
    var p = TenantContext.get();
    List<Invoice> rows;
    if (p != null && p.facilityId() != null) {
      rows = invoices.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    } else if (p != null && p.governorate() != null
        && Set.of("governorate_admin", "directorate_admin", "authority_admin").contains(p.role())) {
      rows = invoices.findByGovernorateOrderByCreatedAtDesc(p.governorate());
    } else {
      rows = invoices.findAllByOrderByCreatedAtDesc();
    }
    var now = java.time.OffsetDateTime.now();
    List<Map<String,Object>> queue = new ArrayList<>();
    BigDecimal outTotal = BigDecimal.ZERO, outInsurer = BigDecimal.ZERO, outPatient = BigDecimal.ZERO;
    BigDecimal over90 = BigDecimal.ZERO, collected = BigDecimal.ZERO;
    int rejected = 0, staleDrafts = 0, drafts = 0, paidCount = 0;
    Map<String,Integer> buckets = new LinkedHashMap<>(Map.of("current",0,"d31_60",0,"d61_90",0,"over_90",0));
    for (var inv : rows) {
      long age = inv.getCreatedAt() == null ? 0 : java.time.Duration.between(inv.getCreatedAt(), now).toDays();
      int rank = FinanceBoardRules.invoiceRank(inv.getStatus(), age);
      if (FinanceBoardRules.receivable(inv.getStatus())) {
        outTotal = outTotal.add(inv.getTotalAmount());
        outInsurer = outInsurer.add(inv.getInsurerAmount());
        outPatient = outPatient.add(inv.getPatientAmount());
        String b = FinanceBoardRules.agingBucket(age);
        buckets.merge(b, 1, Integer::sum);
        if ("over_90".equals(b)) over90 = over90.add(inv.getTotalAmount());
      }
      if ("rejected".equals(inv.getStatus())) rejected++;
      if ("draft".equals(inv.getStatus())) { drafts++; if (age > FinanceBoardRules.DRAFT_STALE_DAYS) staleDrafts++; }
      if ("paid".equals(inv.getStatus())) { paidCount++; collected = collected.add(inv.getTotalAmount()); }
      if (rank == 9) continue;
      var row = new LinkedHashMap<String,Object>();
      row.put("id", inv.getId()); row.put("patient_id", inv.getPatientId());
      row.put("encounter_id", inv.getEncounterId()); row.put("payer", inv.getPayer());
      row.put("status", inv.getStatus()); row.put("total", inv.getTotalAmount());
      row.put("insurer_amount", inv.getInsurerAmount()); row.put("patient_amount", inv.getPatientAmount());
      row.put("age_days", age); row.put("bucket", FinanceBoardRules.agingBucket(age)); row.put("rank", rank);
      queue.add(row);
    }
    queue.sort(Comparator.comparingInt((Map<String,Object> r) -> (Integer) r.get("rank"))
        .thenComparing(r -> -((Number) r.get("age_days")).longValue()));
    var summary = new LinkedHashMap<String,Object>();
    summary.put("invoices_total", rows.size());
    summary.put("attention_invoices", queue.size());
    summary.put("rejected", rejected); summary.put("drafts", drafts); summary.put("drafts_stale", staleDrafts);
    summary.put("paid_count", paidCount); summary.put("collected_total", collected);
    summary.put("outstanding_total", outTotal); summary.put("outstanding_insurer", outInsurer);
    summary.put("outstanding_patient", outPatient); summary.put("overdue_90_total", over90);
    summary.put("aging_buckets", buckets);
    return Map.of("ok", true, "invoices", queue, "summary", summary);
  }
  @GetMapping("/invoices")
  Map<String,Object> list(@RequestParam(required=false) Integer page, @RequestParam(required=false) Integer size) {
    var p = TenantContext.get();
    // Scope follows the caller's tier: a facility account sees its
    // own invoices, a governorate/directorate account sees its
    // governorate, and national/system tiers see everything.
    List<Invoice> rows;
    if (p != null && p.facilityId() != null) {
      rows = invoices.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    } else if (p != null && p.governorate() != null
        && Set.of("governorate_admin", "directorate_admin", "authority_admin").contains(p.role())) {
      rows = invoices.findByGovernorateOrderByCreatedAtDesc(p.governorate());
    } else {
      rows = invoices.findAllByOrderByCreatedAtDesc();
    }
    return Paging.page(rows, "invoices", page, size);
  }
}
