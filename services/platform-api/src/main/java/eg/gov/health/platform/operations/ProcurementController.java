package eg.gov.health.platform.operations;
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
}
