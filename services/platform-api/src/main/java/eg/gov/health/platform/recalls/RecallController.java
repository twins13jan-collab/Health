package eg.gov.health.platform.recalls;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*;
/** Drug batches, stock ledger & recalls (cycle 226). The ledger is
 *  append-only; dispensing obeys expiry, release status and FEFO;
 *  a published recall quarantines its batches everywhere, collects a
 *  receipt per location, and closes only on full disposition; the
 *  regulator notification is "sent" only when ACKed. */
@RestController @RequestMapping("/api/recalls")
public class RecallController {
  private final DrugCatalogItemRepository items; private final DrugBatchRepository batches;
  private final StockMovementRepository movements; private final RecallNoticeRepository notices;
  private final RecallScopeRepository scopes; private final RecallReceiptRepository receipts;
  private final RecallDispositionRepository dispositions; private final AuditService audit;
  public RecallController(DrugCatalogItemRepository items, DrugBatchRepository batches, StockMovementRepository movements,
                          RecallNoticeRepository notices, RecallScopeRepository scopes, RecallReceiptRepository receipts,
                          RecallDispositionRepository dispositions, AuditService audit) {
    this.items=items; this.batches=batches; this.movements=movements; this.notices=notices;
    this.scopes=scopes; this.receipts=receipts; this.dispositions=dispositions; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private DrugBatch batch(UUID id) {
    return batches.findById(id).orElseThrow(() -> BizException.notFound("batch_not_found","التشغيلة غير موجودة"));
  }
  private RecallNotice notice(UUID id) {
    return notices.findById(id).orElseThrow(() -> BizException.notFound("notice_not_found","إشعار الاستدعاء غير موجود"));
  }
  /** Balance rebuilt from the ledger: in minus out, never stored/edited. */
  private long balance(UUID batchId) {
    long bal = 0;
    for (var m : movements.findByBatchIdOrderByCreatedAtAsc(batchId)) {
      switch (m.getMovementType()) {
        case "receive", "return" -> bal += m.getQuantity();
        case "dispense", "destroy" -> bal -= m.getQuantity();
        case "adjustment" -> bal += m.getQuantity(); // signed adjustment
        default -> { }
      }
    }
    return bal;
  }

  public record ItemRequest(String genericName, String tradeName, String form, String strength, String barcode, String registrationRef, String source) {}
  @PostMapping("/items")
  Map<String,Object> item(@RequestBody ItemRequest r) {
    var p = principal();
    if (r.genericName()==null) throw BizException.badRequest("generic_required","الاسم العلمي مطلوب");
    var it = items.save(new DrugCatalogItem(UUID.fromString(p.facilityId()), p.governorate(), r.genericName(),
      r.tradeName(), r.form(), r.strength(), r.barcode(), r.registrationRef(), r.source()));
    audit.record("recall.item.created","drug_catalog_item",it.getId().toString(),"{}");
    return Map.of("ok",true,"item",it);
  }
  public record BatchRequest(UUID itemId, String batchNumber, String manufacturer, String supplier, LocalDate productionDate, LocalDate expiryDate) {}
  @PostMapping("/batches")
  Map<String,Object> batch(@RequestBody BatchRequest r) {
    var p = principal();
    if (r.itemId()==null||r.batchNumber()==null||r.expiryDate()==null)
      throw BizException.badRequest("invalid_batch","الصنف والتشغيلة وتاريخ الانتهاء مطلوبة");
    var b = batches.save(new DrugBatch(UUID.fromString(p.facilityId()), p.governorate(), r.itemId(), r.batchNumber(),
      r.manufacturer(), r.supplier(), r.productionDate(), r.expiryDate()));
    audit.record("recall.batch.created","drug_batch",b.getId().toString(),"{}");
    return Map.of("ok",true,"batch",b);
  }

  public record MovementRequest(String location, String movementType, Integer quantity, String unit, BigDecimal cost,
                                String documentRef, String exceptionReason, String approvedBy) {}
  @PostMapping("/batches/{id}/movements")
  Map<String,Object> move(@PathVariable UUID id, @RequestBody MovementRequest r) {
    var p = principal(); var b = batch(id);
    if (r.location()==null||r.movementType()==null||r.quantity()==null||r.documentRef()==null)
      throw BizException.badRequest("invalid_movement","كل حركة مرتبطة بموقع ونوع وكمية ومستند (دورة 226)");
    if (!RecallRules.movementAllowed(r.movementType(), r.approvedBy()))
      throw BizException.badRequest("adjustment_needs_approval","فرق الجرد ينتج تسوية معتمدة من الجودة — لا تعديل مباشر للرصيد");
    boolean fefoEx = false;
    if ("dispense".equals(r.movementType())) {
      if (!RecallRules.dispensable(b.getReleaseStatus(), b.getExpiryDate(), LocalDate.now()))
        throw BizException.conflict("not_dispensable","منع صرف المنتهي أو المحجور أو المستدعى");
      // FEFO: earliest-expiry released batch of the same item with stock.
      LocalDate earliest = null;
      for (var other : batches.findByItemIdOrderByExpiryDateAsc(b.getItemId())) {
        if ("released".equals(other.getReleaseStatus()) && balance(other.getId()) > 0) { earliest = other.getExpiryDate(); break; }
      }
      fefoEx = RecallRules.fefoException(b.getExpiryDate(), earliest);
      if (!RecallRules.exceptionDocumented(fefoEx, r.exceptionReason()))
        throw BizException.badRequest("fefo_exception_reason","صرف تشغيلة أبعد انتهاءً مع وجود أقرب بالمخزون استثناء FEFO يتطلب سببًا موثقًا");
      if (balance(id) < r.quantity()) throw BizException.conflict("insufficient_stock","الرصيد من دفتر الحركات لا يكفي للصرف");
    }
    var m = movements.save(new StockMovement(b.getFacilityId(), p.governorate(), id, r.location(), r.movementType(),
      r.quantity(), r.unit(), r.cost(), r.documentRef(), fefoEx, r.exceptionReason(), r.approvedBy(), p.email()));
    audit.record("recall.stock."+r.movementType(),"stock_movement",m.getId().toString(),"{}");
    return Map.of("ok",true,"movement",m,"fefo_exception",fefoEx,"balance_after",balance(id));
  }
  @GetMapping("/batches/{id}/balance")
  Map<String,Object> balanceOf(@PathVariable UUID id) {
    batch(id);
    return Map.of("ok",true,"batch_id",id,"balance",balance(id),
      "movements",movements.findByBatchIdOrderByCreatedAtAsc(id).size(),
      "note","balance rebuilt from the append-only ledger — historical entries are never edited");
  }

  public record NoticeRequest(String authority, String reason, String severity, LocalDate effectiveDate, List<UUID> batchIds) {}
  @PostMapping("/notices")
  Map<String,Object> notice(@RequestBody NoticeRequest r) {
    var p = principal();
    if (r.authority()==null||r.reason()==null||r.severity()==null||r.effectiveDate()==null)
      throw BizException.badRequest("invalid_notice","الجهة والسبب والخطورة وتاريخ النفاذ مطلوبة");
    var n = notices.save(new RecallNotice(UUID.fromString(p.facilityId()), p.governorate(), r.authority(), r.reason(), r.severity(), r.effectiveDate()));
    if (r.batchIds()!=null) for (var bid : r.batchIds()) scopes.save(new RecallScope(n.getFacilityId(), p.governorate(), n.getId(), bid));
    audit.record("recall.notice.created","recall_notice",n.getId().toString(),"{}");
    return Map.of("ok",true,"notice",n,"scoped_batches", scopes.findByNoticeId(n.getId()).size());
  }
  @PostMapping("/notices/{id}/publish")
  Map<String,Object> publish(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var n = notice(id);
    if (!"draft".equals(n.getStatus())) throw BizException.conflict("not_draft","الإشعار ليس مسودة");
    String by = body.get("publishedBy");
    if (by==null||by.isBlank()) throw BizException.badRequest("publisher_required","مسؤول الاستدعاء ينشر الإشعار المعتمد");
    n.publish(by); notices.save(n);
    // Publishing quarantines every scoped batch: dispensing stops everywhere.
    for (var s : scopes.findByNoticeId(id)) {
      var b = batch(s.getBatchId());
      b.setReleaseStatus("recalled"); batches.save(b);
    }
    audit.record("recall.notice.published","recall_notice",id.toString(),"{}");
    return Map.of("ok",true,"notice",n,"dispensing_blocked",true,
      "regulator_notified",false,"note","external notification stays unconfirmed until ACK");
  }
  @PostMapping("/notices/{id}/regulator-ack")
  Map<String,Object> ack(@PathVariable UUID id) {
    var n = notice(id); n.ack(); notices.save(n);
    audit.record("recall.notice.regulator_ack","recall_notice",id.toString(),"{}");
    return Map.of("ok",true,"notice",n,"regulator_notified",true);
  }
  public record ReceiptRequest(String location, String acknowledgedBy, Integer quarantinedQty) {}
  @PostMapping("/notices/{id}/receipts")
  Map<String,Object> receipt(@PathVariable UUID id, @RequestBody ReceiptRequest r) {
    var p = principal(); var n = notice(id);
    if (!"published".equals(n.getStatus())) throw BizException.conflict("not_published","الإقرارات على إشعار منشور فقط");
    if (r.location()==null||r.acknowledgedBy()==null) throw BizException.badRequest("invalid_receipt","الموقع والمقر بالاستلام مطلوبان");
    var rec = receipts.save(new RecallReceipt(n.getFacilityId(), p.governorate(), id, r.location(), r.acknowledgedBy(),
      r.quarantinedQty()==null?0:r.quarantinedQty()));
    audit.record("recall.receipt.acknowledged","recall_receipt",rec.getId().toString(),"{}");
    return Map.of("ok",true,"receipt",rec);
  }
  public record DispositionRequest(UUID batchId, String disposition, Integer quantity, String approvedBy) {}
  @PostMapping("/notices/{id}/dispositions")
  Map<String,Object> disposition(@PathVariable UUID id, @RequestBody DispositionRequest r) {
    var p = principal(); var n = notice(id);
    // Quality approves quarantine, release and disposition — always.
    if (r.batchId()==null||r.disposition()==null||r.quantity()==null||r.approvedBy()==null||r.approvedBy().isBlank())
      throw BizException.badRequest("invalid_disposition","التصرف (إرجاع/إتلاف) واعتماد الجودة مطلوبان");
    var d = dispositions.save(new RecallDisposition(n.getFacilityId(), p.governorate(), id, r.batchId(), r.disposition(), r.quantity(), r.approvedBy()));
    audit.record("recall.disposition.executed","recall_disposition",d.getId().toString(),"{}");
    return Map.of("ok",true,"disposition",d);
  }
  @PostMapping("/notices/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    var n = notice(id);
    long quarantined = receipts.findByNoticeId(id).stream().mapToLong(RecallReceipt::getQuarantinedQty).sum();
    long disposed = dispositions.findByNoticeId(id).stream().mapToLong(RecallDisposition::getQuantity).sum();
    if (!RecallRules.recallClosable(quarantined, disposed))
      throw BizException.conflict("dispositions_incomplete","لا إغلاق قبل تصرف نهائي يغطي كل الكميات المحجوزة المقر بها");
    n.close(); notices.save(n);
    audit.record("recall.notice.closed","recall_notice",id.toString(),"{}");
    return Map.of("ok",true,"notice",n,"quarantined_total",quarantined,"disposed_total",disposed);
  }

  @GetMapping("/batches")
  Map<String,Object> byItem(@RequestParam UUID itemId) {
    var rows = batches.findByItemIdOrderByExpiryDateAsc(itemId);
    return Map.of("ok",true,"batches",rows,"count",rows.size());
  }
}
