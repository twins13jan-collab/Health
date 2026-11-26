package eg.gov.health.platform.supplychaindeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Supply chains & logistics risk (module 68). Suppliers,
 *  manufacturers, distributors, supply sources and regulatory
 *  accreditation; purchase orders, shipments, receipt, storage,
 *  distribution and returns tracked; temperature, cold chain
 *  and batch and serial traceability; single-supplier and
 *  single-route dependence risk analyzed with alternatives
 *  identified; emergency stock, rare medicines, blood
 *  components, oxygen and critical supplies governed; and
 *  supply-disruption alerts, distribution and reallocation
 *  under documented controls. */
@RestController @RequestMapping("/api/supply-chain-deep")
public class SupplyChainDeepController {
  private final SupplySourceRepository sources; private final SupplyRiskAssessmentRepository risks; private final AuditService audit;
  public SupplyChainDeepController(SupplySourceRepository sources, SupplyRiskAssessmentRepository risks, AuditService audit) {
    this.sources=sources; this.risks=risks; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record SourceBody(String itemRef, String supplierRef, Boolean regulatoryApproved, Integer sharePercent) {}
  @PostMapping("/sources")
  Map<String,Object> source(@RequestBody SourceBody r) {
    var p = principal();
    if (!Boolean.TRUE.equals(r.regulatoryApproved()))
      throw BizException.conflict("unapproved_source","مصدر توريد يمد الرعاية فقط باعتماد تنظيمي");
    var s = sources.save(new SupplySource(UUID.fromString(p.facilityId()), p.governorate(), r.itemRef(), r.supplierRef(), true, r.sharePercent()));
    return Map.of("ok",true,"source",s);
  }
  @GetMapping("/items/{itemRef}/dependence")
  Map<String,Object> dependence(@PathVariable String itemRef) {
    principal();
    int maxShare = sources.findByItemRef(itemRef).stream()
      .mapToInt(s -> s.getSharePercent() == null ? 0 : s.getSharePercent()).max().orElse(0);
    return Map.of("ok",true,"item_ref",itemRef,"max_single_source_share_percent",maxShare,
      "high_dependence",SupplyRules.highDependence(maxShare));
  }
  public record RiskBody(String itemRef, Boolean alternativeIdentified) {}
  @PostMapping("/risk-assessments")
  Map<String,Object> assess(@RequestBody RiskBody r) {
    var p = principal();
    int maxShare = sources.findByItemRef(r.itemRef()).stream()
      .mapToInt(s -> s.getSharePercent() == null ? 0 : s.getSharePercent()).max().orElse(0);
    boolean high = SupplyRules.highDependence(maxShare);
    boolean managed = SupplyRules.riskManaged(high, Boolean.TRUE.equals(r.alternativeIdentified()));
    var a = risks.save(new SupplyRiskAssessment(UUID.fromString(p.facilityId()), p.governorate(), r.itemRef(),
      Boolean.TRUE.equals(r.alternativeIdentified())));
    if (managed) { a.markManaged(); risks.save(a); }
    return Map.of("ok",true,"assessment",a,"high_dependence",high,"managed",managed,
      "note","a risk register that lists fragility without alternatives is a diary, not a defence");
  }
  @GetMapping("/emergency-stock")
  Map<String,Object> emergency(@RequestParam int floorUnits, @RequestParam int onHandUnits) {
    principal();
    return Map.of("ok",true,"floor_units",floorUnits,"on_hand_units",onHandUnits,
      "shortfall",SupplyRules.emergencyShortfall(floorUnits, onHandUnits));
  }
}
