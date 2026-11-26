package eg.gov.health.platform.opendata;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Open-data governance & statistics (module 188). The
 *  datasets permitted for publication are defined;
 *  re-identification risk assessed; aggregation and
 *  anonymization governed; definitions, methodology and data
 *  source documented; reuse terms and updates managed; and
 *  published data withdrawn when an error or a privacy risk is
 *  discovered. */
@RestController @RequestMapping("/api/open-data")
public class OpenDataController {
  private final OpenDatasetRepository datasets; private final AuditService audit;
  public OpenDataController(OpenDatasetRepository datasets, AuditService audit) {
    this.datasets=datasets; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private OpenDataset dataset(UUID id) {
    return datasets.findById(id).orElseThrow(() -> BizException.notFound("dataset_not_found","المجموعة غير موجودة"));
  }

  public record DsBody(String name, Integer minCellSize, Boolean reidentificationRiskAssessed, Boolean anonymized, Boolean definitionsDocumented) {}
  @PostMapping("/datasets")
  Map<String,Object> create(@RequestBody DsBody r) {
    var p = principal();
    var d = datasets.save(new OpenDataset(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.minCellSize(),
      Boolean.TRUE.equals(r.reidentificationRiskAssessed()), Boolean.TRUE.equals(r.anonymized()), Boolean.TRUE.equals(r.definitionsDocumented())));
    return Map.of("ok",true,"dataset",d);
  }
  @PostMapping("/datasets/{id}/publish")
  Map<String,Object> publish(@PathVariable UUID id) {
    principal(); var d = dataset(id);
    var blockers = OpenDataRules.publishBlockers(d.isReidentificationRiskAssessed(), d.isAnonymized(), d.getMinCellSize(), d.isDefinitionsDocumented());
    if (!blockers.isEmpty())
      throw BizException.conflict("not_publishable","مجموعة بيانات مفتوحة تقف على: " + String.join("، ", blockers) + " — دون حد الخلية الأدنى قد تكون «الإحصائية» شخصًا مسمى");
    d.publish(); datasets.save(d);
    return Map.of("ok",true,"dataset",d);
  }
  public record WithdrawBody(String reason) {}
  @PostMapping("/datasets/{id}/withdraw")
  Map<String,Object> withdraw(@PathVariable UUID id, @RequestBody WithdrawBody r) {
    principal(); var d = dataset(id);
    if (r.reason()==null || r.reason().isBlank())
      throw BizException.badRequest("reason_required","سحب بيانات منشورة يكون فوريًا وبسبب مسجل عند اكتشاف خطأ أو خطر خصوصية");
    d.withdraw(r.reason()); datasets.save(d);
    audit.record("opendata.dataset.withdrawn","open_dataset",id.toString(),"{}");
    return Map.of("ok",true,"dataset",d,"withdrawn",true);
  }
}
