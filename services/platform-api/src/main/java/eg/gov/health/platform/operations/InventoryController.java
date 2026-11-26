package eg.gov.health.platform.operations;
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
}
