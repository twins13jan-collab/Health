package eg.gov.health.platform.clinicalcosting;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Clinical costing & patient pathway (module 165). Direct and
 *  indirect cost of service, stay and medication is computed;
 *  resource consumption is linked to ACTUAL execution; pathway
 *  cost is analysed across departments; variance from the
 *  standard cost is analysed; and value is measured WITHOUT
 *  reducing the medical decision to its cost. */
@RestController @RequestMapping("/api/clinical-costing")
public class ClinicalCostingController {
  private final PathwayCostCaseRepository cases; private final CostEntryRepository entries; private final AuditService audit;
  public ClinicalCostingController(PathwayCostCaseRepository cases, CostEntryRepository entries, AuditService audit) {
    this.cases=cases; this.entries=entries; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private PathwayCostCase costCase(UUID id) {
    return cases.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","الحالة غير موجودة"));
  }
  private double actualTotal(UUID caseId) {
    return entries.findByCaseId(caseId).stream().filter(CostEntry::isActuallyConsumed)
      .mapToDouble(e -> e.getAmount() == null ? 0.0 : e.getAmount()).sum();
  }

  public record CaseBody(String patientRef, String pathwayRef, Double standardCost) {}
  @PostMapping("/cases")
  Map<String,Object> create(@RequestBody CaseBody r) {
    var p = principal();
    var c = cases.save(new PathwayCostCase(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.pathwayRef(), r.standardCost()));
    return Map.of("ok",true,"cost_case",c);
  }
  public record EntryBody(String costType, String category, Double amount, Boolean actuallyConsumed) {}
  @PostMapping("/cases/{id}/entries")
  Map<String,Object> entry(@PathVariable UUID id, @RequestBody EntryBody r) {
    var p = principal(); costCase(id);
    var e = entries.save(new CostEntry(UUID.fromString(p.facilityId()), p.governorate(), id, r.costType(), r.category(), r.amount(), Boolean.TRUE.equals(r.actuallyConsumed())));
    return Map.of("ok",true,"entry",e,"counted",Boolean.TRUE.equals(r.actuallyConsumed()),
      "note","only actually-consumed resources enter the pathway cost");
  }
  @GetMapping("/cases/{id}/summary")
  Map<String,Object> summary(@PathVariable UUID id) {
    principal(); var c = costCase(id);
    double total = actualTotal(id);
    double variance = CostingRules.variancePct(total, c.getStandardCost());
    boolean review = CostingRules.varianceReviewNeeded(total, c.getStandardCost());
    if (review && "none".equals(c.getVarianceReviewStatus())) { c.setVarianceReviewStatus("open"); cases.save(c); }
    return Map.of("ok",true,"case_id",id.toString(),"actual_total",total,"standard_cost",c.getStandardCost() == null ? 0.0 : c.getStandardCost(),
      "variance_pct",variance,"variance_review",c.getVarianceReviewStatus(),
      "value_guard","costing informs the decision — it never makes it, and never recommends withholding care");
  }
  @PostMapping("/cases/{id}/close-variance-review")
  Map<String,Object> closeReview(@PathVariable UUID id) {
    var p = principal(); var c = costCase(id);
    if (!CostingRules.mayCloseVarianceReview(p.role()))
      throw BizException.forbidden("مراجعة انحراف تكلفة سريرية يغلقها سريري لا المالية وحدها — القرار الطبي لا يُختزل في سعره");
    c.setVarianceReviewStatus("closed"); cases.save(c);
    audit.record("costing.variance_review.closed","pathway_cost_case",id.toString(),"{}");
    return Map.of("ok",true,"cost_case",c);
  }
}
