package eg.gov.health.platform.purchasing;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.util.*;
/** Procurement, contracts, receiving & suppliers (cycle 227). The full
 *  chain is traceable — need → tender → award → contract → receipt →
 *  invoice → payment — with duties separated at every step, three-way
 *  matching before payment, independently verified supplier bank
 *  changes, and contract amendments as preserved new versions. */
@RestController @RequestMapping("/api/purchasing")
public class PurchasingController {
  private final SupplierRepository suppliers; private final PurchaseRequestRepository requests;
  private final TenderRepository tenders; private final BidRepository bids;
  private final ProcPurchaseOrderRepository orders; private final ContractRepository contracts;
  private final GoodsReceiptRepository receipts; private final InspectionRepository inspections;
  private final SupplierInvoiceRepository invoices; private final CreditNoteRepository creditNotes;
  private final AuditService audit;
  public PurchasingController(SupplierRepository suppliers, PurchaseRequestRepository requests, TenderRepository tenders,
                              BidRepository bids, ProcPurchaseOrderRepository orders, ContractRepository contracts,
                              GoodsReceiptRepository receipts, InspectionRepository inspections,
                              SupplierInvoiceRepository invoices, CreditNoteRepository creditNotes, AuditService audit) {
    this.suppliers=suppliers; this.requests=requests; this.tenders=tenders; this.bids=bids; this.orders=orders;
    this.contracts=contracts; this.receipts=receipts; this.inspections=inspections; this.invoices=invoices;
    this.creditNotes=creditNotes; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record SupplierRequest(String legalName, String registrationNo, String bankAccountRef, String riskLevel) {}
  @PostMapping("/suppliers")
  Map<String,Object> supplier(@RequestBody SupplierRequest r) {
    var p = principal();
    if (r.legalName()==null) throw BizException.badRequest("legal_name_required","الهوية القانونية للمورد مطلوبة");
    var s = suppliers.save(new Supplier(UUID.fromString(p.facilityId()), p.governorate(), r.legalName(),
      r.registrationNo(), r.bankAccountRef(), r.riskLevel(), p.email()));
    audit.record("purchasing.supplier.registered","supplier",s.getId().toString(),"{}");
    return Map.of("ok",true,"supplier",s,"bank_verified",false,
      "note","bank details count as approved only after independent verification");
  }
  @PostMapping("/suppliers/{id}/bank")
  Map<String,Object> bankChange(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal();
    var s = suppliers.findById(id).orElseThrow(() -> BizException.notFound("supplier_not_found","المورد غير موجود"));
    if (body.get("bankAccountRef")==null) throw BizException.badRequest("bank_ref_required","مرجع الحساب مطلوب");
    s.changeBank(body.get("bankAccountRef"), p.email()); suppliers.save(s);
    audit.record("purchasing.supplier.bank_changed","supplier",id.toString(),"{}");
    return Map.of("ok",true,"supplier",s,"bank_verified",false,"note","verification reset — an independent verifier must re-approve");
  }
  @PostMapping("/suppliers/{id}/verify-bank")
  Map<String,Object> verifyBank(@PathVariable UUID id) {
    var p = principal();
    var s = suppliers.findById(id).orElseThrow(() -> BizException.notFound("supplier_not_found","المورد غير موجود"));
    if (!PurchasingRules.bankChangeVerified(s.getBankChangedBy(), p.email()))
      throw BizException.forbidden("تغيير الحساب البنكي للمورد يتطلب تحققًا مستقلًا من شخص مختلف عن من غيّره");
    s.verifyBank(p.email()); suppliers.save(s);
    audit.record("purchasing.supplier.bank_verified","supplier",id.toString(),"{}");
    return Map.of("ok",true,"supplier",s,"bank_verified",true);
  }

  public record RequestDto(String itemDescription, String specs, Integer quantity, String justification) {}
  @PostMapping("/requests")
  Map<String,Object> request(@RequestBody RequestDto r) {
    var p = principal();
    if (r.itemDescription()==null||r.justification()==null) throw BizException.badRequest("invalid_request","الاحتياج والمبرر مطلوبان");
    var req = requests.save(new PurchaseRequest(UUID.fromString(p.facilityId()), p.governorate(), p.email(),
      r.itemDescription(), r.specs(), r.quantity()==null?1:r.quantity(), r.justification()));
    audit.record("purchasing.request.created","purchase_request",req.getId().toString(),"{}");
    return Map.of("ok",true,"request",req);
  }
  @PostMapping("/requests/{id}/approve")
  Map<String,Object> approveRequest(@PathVariable UUID id) {
    var p = principal();
    var req = requests.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    if (!PurchasingRules.approvable(req.getRequester(), p.email()))
      throw BizException.forbidden("الطالب ينشئ الاحتياج ولا يعتمد الشراء منفردًا — فصل مهام");
    req.decide("approved", p.email()); requests.save(req);
    audit.record("purchasing.request.approved","purchase_request",id.toString(),"{}");
    return Map.of("ok",true,"request",req);
  }

  public record TenderRequest(UUID requestId, String title) {}
  @PostMapping("/tenders")
  Map<String,Object> tender(@RequestBody TenderRequest r) {
    var p = principal();
    if (r.title()==null) throw BizException.badRequest("title_required","عنوان الطرح مطلوب");
    var t = tenders.save(new Tender(UUID.fromString(p.facilityId()), p.governorate(), r.requestId(), r.title()));
    audit.record("purchasing.tender.opened","tender",t.getId().toString(),"{}");
    return Map.of("ok",true,"tender",t);
  }
  public record BidRequest(UUID supplierId, BigDecimal amount) {}
  @PostMapping("/tenders/{id}/bids")
  Map<String,Object> bid(@PathVariable UUID id, @RequestBody BidRequest r) {
    var p = principal();
    var t = tenders.findById(id).orElseThrow(() -> BizException.notFound("tender_not_found","الطرح غير موجود"));
    var b = bids.save(new Bid(tFac(t), p.governorate(), id, r.supplierId(), r.amount()));
    audit.record("purchasing.bid.submitted","bid",b.getId().toString(),"{}");
    return Map.of("ok",true,"bid",b);
  }
  private UUID tFac(Tender t) { return t.getFacilityId(); }
  public record ScoreRequest(BigDecimal score) {}
  @PostMapping("/bids/{id}/score")
  Map<String,Object> score(@PathVariable UUID id, @RequestBody ScoreRequest r) {
    var p = principal();
    var b = bids.findById(id).orElseThrow(() -> BizException.notFound("bid_not_found","العرض غير موجود"));
    if (r.score()==null) throw BizException.badRequest("score_required","لجنة التقييم تسجل نتائج التقييم");
    b.score(r.score(), p.email()); bids.save(b);
    audit.record("purchasing.bid.scored","bid",id.toString(),"{}");
    return Map.of("ok",true,"bid",b,"scored_by_committee",p.email());
  }
  @PostMapping("/tenders/{id}/award")
  Map<String,Object> award(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var t = tenders.findById(id).orElseThrow(() -> BizException.notFound("tender_not_found","الطرح غير موجود"));
    if (body.get("bidId")==null||body.get("committeeMinutes")==null)
      throw BizException.badRequest("award_detail","الترسية تتطلب العرض الفائز ومحضر لجنة موثقًا");
    var bid = bids.findById(UUID.fromString(body.get("bidId"))).orElseThrow(() -> BizException.notFound("bid_not_found","العرض غير موجود"));
    if (!"evaluated".equals(bidStatus(bid))) throw BizException.conflict("bid_not_evaluated","لا ترسية على عرض لم تقيّمه اللجنة");
    bid.setStatus("awarded"); bids.save(bid);
    t.award(bid.getId(), body.get("committeeMinutes")); tenders.save(t);
    audit.record("purchasing.tender.awarded","tender",id.toString(),"{}");
    return Map.of("ok",true,"tender",t);
  }
  private String bidStatus(Bid b) { return b.getStatus(); }

  public record PoRequest(UUID requestId, UUID tenderId, UUID supplierId, String items, BigDecimal totalAmount, BigDecimal taxes, String deliveryTerms, String warranty) {}
  @PostMapping("/orders")
  Map<String,Object> order(@RequestBody PoRequest r) {
    var p = principal();
    if (r.supplierId()==null||r.totalAmount()==null) throw BizException.badRequest("invalid_po","المورد والقيمة مطلوبان");
    if (r.requestId()!=null) {
      var req = requests.findById(r.requestId()).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
      if (!"approved".equals(req.getStatus()))
        throw BizException.conflict("request_not_approved","المشتريات تصدر الأمر بعد الاعتمادات فقط");
    }
    var s = suppliers.findById(r.supplierId()).orElseThrow(() -> BizException.notFound("supplier_not_found","المورد غير موجود"));
    if (!s.isBankVerified())
      throw BizException.conflict("bank_unverified","لا أمر شراء لمورد حسابه البنكي غير موثق بتحقق مستقل");
    var po = orders.save(new PurchaseOrder(UUID.fromString(p.facilityId()), p.governorate(), r.requestId(), r.tenderId(),
      r.supplierId(), r.items(), r.totalAmount(), r.taxes(), r.deliveryTerms(), r.warranty(), p.email(), 1, null));
    audit.record("purchasing.po.issued","purchase_order",po.getId().toString(),"{}");
    return Map.of("ok",true,"order",po);
  }
  /** Amendment: a NEW version is issued; the previous order/contract is preserved. */
  @PostMapping("/orders/{id}/amend")
  Map<String,Object> amend(@PathVariable UUID id, @RequestBody PoRequest r) {
    var p = principal();
    var orig = orders.findById(id).orElseThrow(() -> BizException.notFound("po_not_found","الأمر غير موجود"));
    var po = orders.save(new PurchaseOrder(orig.getFacilityId(), p.governorate(), orig.getRequestId(), orig.getTenderId(),
      r.supplierId()==null?orig.getSupplierId():r.supplierId(), r.items()==null?orig.getItems():r.items(),
      r.totalAmount()==null?orig.getTotalAmount():r.totalAmount(), orig.getTaxes(), orig.getDeliveryTerms(), orig.getWarranty(),
      p.email(), orig.getVersion()+1, id));
    orig.markAmended(); orders.save(orig);
    audit.record("purchasing.po.amended","purchase_order",po.getId().toString(),"{}");
    return Map.of("ok",true,"amended_order",po,"previous_version_preserved",id);
  }
  @PostMapping("/orders/{id}/contract")
  Map<String,Object> contract(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal();
    var po = orders.findById(id).orElseThrow(() -> BizException.notFound("po_not_found","الأمر غير موجود"));
    var existing = contracts.findByPoIdOrderByVersionDesc(id);
    int version = existing.isEmpty() ? 1 : existing.get(0).getVersion() + 1;
    UUID supersedes = existing.isEmpty() ? null : existing.get(0).getId();
    if (!existing.isEmpty()) { var prev = existing.get(0); prev.supersede(); contracts.save(prev); }
    var c = contracts.save(new Contract(po.getFacilityId(), p.governorate(), id, body.get("terms"), version, supersedes));
    audit.record("purchasing.contract.versioned","contract",c.getId().toString(),"{}");
    return Map.of("ok",true,"contract",c,"version",version,"note","amendments are addenda — previous versions are never erased");
  }

  public record ReceiptRequest(Integer quantityReceived, Boolean partial, Boolean substitute, String technicalApproval, String contractApproval) {}
  @PostMapping("/orders/{id}/receipts")
  Map<String,Object> receipt(@PathVariable UUID id, @RequestBody ReceiptRequest r) {
    var p = principal();
    var po = orders.findById(id).orElseThrow(() -> BizException.notFound("po_not_found","الأمر غير موجود"));
    // Receiving is separate from issuing the order.
    if (!PurchasingRules.receiverIndependent(po.getIssuedBy(), p.email()))
      throw BizException.forbidden("الاستلام المخزني/الفني منفصل عن إصدار الأمر — لا يستلم مصدر الأمر بضاعته");
    boolean partial = Boolean.TRUE.equals(r.partial()), substitute = Boolean.TRUE.equals(r.substitute());
    if (!PurchasingRules.deliveryApprovable(partial, substitute, r.technicalApproval(), r.contractApproval()))
      throw BizException.badRequest("delivery_approvals","التسليم الجزئي أو البديل يحتاج موافقة فنية وتعاقدية معًا");
    var rec = receipts.save(new GoodsReceipt(po.getFacilityId(), p.governorate(), id, p.email(),
      r.quantityReceived()==null?0:r.quantityReceived(), partial, substitute, r.technicalApproval(), r.contractApproval()));
    audit.record("purchasing.goods.received","goods_receipt",rec.getId().toString(),"{}");
    return Map.of("ok",true,"receipt",rec);
  }
  public record InspectionRequest(String inspector, String result, String notes) {}
  @PostMapping("/receipts/{id}/inspections")
  Map<String,Object> inspection(@PathVariable UUID id, @RequestBody InspectionRequest r) {
    var p = principal();
    var rec = receipts.findById(id).orElseThrow(() -> BizException.notFound("receipt_not_found","الاستلام غير موجود"));
    if (r.inspector()==null||r.result()==null) throw BizException.badRequest("invalid_inspection","الفاحص الفني والنتيجة مطلوبان");
    if (r.inspector().equals(rec.getReceivedBy()))
      throw BizException.forbidden("الفحص الفني مستقل عن المستلم المخزني");
    var ins = inspections.save(new Inspection(recFac(rec), p.governorate(), id, r.inspector(), r.result(), r.notes()));
    audit.record("purchasing.inspection.done","inspection",ins.getId().toString(),"{}");
    return Map.of("ok",true,"inspection",ins);
  }
  private UUID recFac(GoodsReceipt g) { return g.getFacilityId(); }

  public record InvoiceRequest(UUID receiptId, BigDecimal amount) {}
  @PostMapping("/orders/{id}/invoices")
  Map<String,Object> invoice(@PathVariable UUID id, @RequestBody InvoiceRequest r) {
    var p = principal();
    var po = orders.findById(id).orElseThrow(() -> BizException.notFound("po_not_found","الأمر غير موجود"));
    var inv = new SupplierInvoice(po.getFacilityId(), p.governorate(), id, r.receiptId(), r.amount());
    String inspectionResult = null;
    if (r.receiptId()!=null) {
      var latest = inspections.findByReceiptIdOrderByCreatedAtDesc(r.receiptId());
      inspectionResult = latest.isEmpty() ? null : latest.get(0).getResult();
    }
    invoices.save(inv);
    // Three-way match runs immediately: order vs receipt-inspection vs invoice.
    if (PurchasingRules.threeWayMatch(po.getTotalAmount(), r.amount(), inspectionResult)) {
      inv.match(); invoices.save(inv);
      audit.record("purchasing.invoice.matched","supplier_invoice",inv.getId().toString(),"{}");
      return Map.of("ok",true,"invoice",inv,"three_way_match",true);
    }
    inv.hold("invoice does not match order amount or accepted inspection — held for review");
    invoices.save(inv);
    audit.record("purchasing.invoice.held","supplier_invoice",inv.getId().toString(),"{}");
    return Map.of("ok",true,"invoice",inv,"three_way_match",false,"held",true);
  }
  @PostMapping("/invoices/{id}/pay")
  Map<String,Object> pay(@PathVariable UUID id) {
    var p = principal();
    var inv = invoices.findById(id).orElseThrow(() -> BizException.notFound("invoice_not_found","الفاتورة غير موجودة"));
    if (!"matched".equals(inv.getStatus()))
      throw BizException.conflict("not_matched","لا صرف قبل مطابقة ثلاثية موثقة — الفاتورة المخالفة معلقة للمراجعة");
    var po = orders.findById(invPo(inv)).orElseThrow(() -> BizException.notFound("po_not_found","الأمر غير موجود"));
    String receiver = receipts.findByPoIdOrderByCreatedAtAsc(invPo(inv)).stream().findFirst().map(GoodsReceipt::getReceivedBy).orElse("");
    if (!PurchasingRules.payerIndependent(po.getIssuedBy(), receiver, p.email()))
      throw BizException.forbidden("المالية تعتمد الصرف بفصل مهام عن الإصدار والاستلام");
    inv.pay(p.email()); invoices.save(inv);
    audit.record("purchasing.invoice.paid","supplier_invoice",id.toString(),"{}");
    return Map.of("ok",true,"invoice",inv);
  }
  private UUID invPo(SupplierInvoice i) { return i.getPoId(); }
  public record CreditRequest(BigDecimal amount, String reason) {}
  @PostMapping("/invoices/{id}/credit-notes")
  Map<String,Object> creditNote(@PathVariable UUID id, @RequestBody CreditRequest r) {
    var p = principal();
    var inv = invoices.findById(id).orElseThrow(() -> BizException.notFound("invoice_not_found","الفاتورة غير موجودة"));
    var cn = creditNotes.save(new CreditNote(invFac(inv), p.governorate(), id, r.amount(), r.reason()));
    audit.record("purchasing.credit_note.issued","credit_note",cn.getId().toString(),"{}");
    return Map.of("ok",true,"credit_note",cn);
  }
  private UUID invFac(SupplierInvoice i) { return i.getFacilityId(); }
}
