package eg.gov.health.platform.vendormonitoring;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Vendor monitoring & supply-chain risk (module 189).
 *  Single-vendor dependence assessed; alternative vendors and
 *  supply sources managed; delivery-date and quality compliance
 *  tracked; manufacturing, transport and import disruption
 *  monitored; emergency plans and reallocation governed; and
 *  exceptional-purchase decisions documented and reviewed. */
@RestController @RequestMapping("/api/vendor-monitoring")
public class VendorMonitoringController {
  private final VendorPerformanceRepository performances; private final ExceptionalPurchaseRepository exceptionals; private final AuditService audit;
  public VendorMonitoringController(VendorPerformanceRepository performances, ExceptionalPurchaseRepository exceptionals, AuditService audit) {
    this.performances=performances; this.exceptionals=exceptionals; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record PerfBody(String vendorRef, Double onTimePercent, Double qualityScore) {}
  @PostMapping("/performance")
  Map<String,Object> performance(@RequestBody PerfBody r) {
    var p = principal();
    var v = performances.save(new VendorPerformance(UUID.fromString(p.facilityId()), p.governorate(), r.vendorRef(), r.onTimePercent(), r.qualityScore()));
    return Map.of("ok",true,"performance",v,
      "at_risk",VendorRules.atRisk(r.onTimePercent() == null ? 0 : r.onTimePercent(), r.qualityScore() == null ? 0 : r.qualityScore()),
      "note","standing is computed from deliveries — on-time under 90% or quality under 80 flags the vendor, not relationships");
  }
  public record ExcBody(String itemRef, String vendorRef, String reason, Boolean documented) {}
  @PostMapping("/exceptional-purchases")
  Map<String,Object> exceptional(@RequestBody ExcBody r) {
    var p = principal();
    var e = exceptionals.save(new ExceptionalPurchase(UUID.fromString(p.facilityId()), p.governorate(), r.itemRef(), r.vendorRef(),
      r.reason(), Boolean.TRUE.equals(r.documented())));
    return Map.of("ok",true,"purchase",e,"legitimate",VendorRules.exceptionalLegitimate(e.isDocumented(), e.isReviewed()),
      "note","an exceptional purchase is legitimate only documented AND reviewed after the fact");
  }
  @PostMapping("/exceptional-purchases/{id}/review")
  Map<String,Object> review(@PathVariable UUID id) {
    principal();
    var e = exceptionals.findById(id).orElseThrow(() -> BizException.notFound("purchase_not_found","الشراء غير موجود"));
    e.review(); exceptionals.save(e);
    audit.record("vendor.exceptional.reviewed","exceptional_purchase",id.toString(),"{}");
    return Map.of("ok",true,"purchase",e,"legitimate",VendorRules.exceptionalLegitimate(e.isDocumented(), e.isReviewed()));
  }
}
