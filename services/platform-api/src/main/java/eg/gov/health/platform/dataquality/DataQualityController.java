package eg.gov.health.platform.dataquality;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Data quality, definitions & indicators (module 122). Rules
 *  for completeness, validity, consistency, freshness and
 *  de-duplication; a named data owner and source of truth for
 *  every asset; a data dictionary and indicator definitions;
 *  missing and conflicting data tracked through correction; and
 *  the confidence grade and caveats shown with every figure. */
@RestController @RequestMapping("/api/data-quality")
public class DataQualityController {
  private final DataAssetRepository assets; private final DqIndicatorDefinitionRepository indicators;
  private final IndicatorPublicationRepository publications; private final DataCorrectionRepository corrections; private final AuditService audit;
  public DataQualityController(DataAssetRepository assets, DqIndicatorDefinitionRepository indicators,
                               IndicatorPublicationRepository publications, DataCorrectionRepository corrections, AuditService audit) {
    this.assets=assets; this.indicators=indicators; this.publications=publications; this.corrections=corrections; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private DataAsset asset(UUID id) {
    return assets.findById(id).orElseThrow(() -> BizException.notFound("asset_not_found","الأصل غير موجود"));
  }

  public record AssetBody(String name, String ownerRef, String sourceOfTruth, Double completenessPct, Double validityPct, Double consistencyPct, Double freshnessHours) {}
  @PostMapping("/assets")
  Map<String,Object> create(@RequestBody AssetBody r) {
    var p = principal();
    if (r.ownerRef() == null || r.ownerRef().isBlank() || r.sourceOfTruth() == null || r.sourceOfTruth().isBlank())
      throw BizException.badRequest("owner_required","أصل بيانات بلا مالك مسمى ومصدر حقيقة معلن بيانات يتيمة");
    var a = assets.save(new DataAsset(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.ownerRef(), r.sourceOfTruth(),
      r.completenessPct(), r.validityPct(), r.consistencyPct(), r.freshnessHours()));
    double conf = DataQualityRules.confidence(r.completenessPct(), r.validityPct(), r.consistencyPct(), r.freshnessHours());
    return Map.of("ok",true,"asset",a,"confidence_score",conf,"low_confidence",conf < DataQualityRules.LOW_CONFIDENCE_BELOW);
  }
  public record IndicatorBody(String name, String definition, String numerator, String denominator, String ownerRef) {}
  @PostMapping("/indicators")
  Map<String,Object> indicator(@RequestBody IndicatorBody r) {
    var p = principal();
    var i = indicators.save(new DqIndicatorDefinition(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.definition(), r.numerator(), r.denominator(), r.ownerRef()));
    return Map.of("ok",true,"indicator",i);
  }
  public record PublishBody(UUID assetId, Double value, String caveat) {}
  @PostMapping("/indicators/{id}/publish")
  Map<String,Object> publish(@PathVariable UUID id, @RequestBody PublishBody r) {
    var p = principal();
    indicators.findById(id).orElseThrow(() -> BizException.notFound("indicator_not_found","المؤشر غير موجود"));
    var a = asset(r.assetId());
    double conf = DataQualityRules.confidence(a.getCompletenessPct(), a.getValidityPct(), a.getConsistencyPct(), a.getFreshnessHours());
    if (!DataQualityRules.publishable(conf, r.caveat()))
      throw BizException.conflict("low_confidence_no_caveat","مؤشر بثقة دون 70 لا يُنشر بلا تحفظاته مرفقة — رقم دقيق المظهر على بيانات فاسدة أسوأ من غيابه");
    var pub = publications.save(new IndicatorPublication(UUID.fromString(p.facilityId()), p.governorate(), id, r.assetId(), r.value(), conf, r.caveat()));
    audit.record("dq.indicator.published","indicator_publication",pub.getId().toString(),"{}");
    return Map.of("ok",true,"publication",pub,"confidence_score",conf);
  }
  public record CorrectionBody(String issueType, String description, String resolutionBasis) {}
  @PostMapping("/assets/{id}/corrections")
  Map<String,Object> correct(@PathVariable UUID id, @RequestBody CorrectionBody r) {
    var p = principal(); asset(id);
    boolean done = r.resolutionBasis() != null && !r.resolutionBasis().isBlank();
    if ("conflicting".equals(r.issueType()) && !done)
      throw BizException.badRequest("basis_required","تصحيح سجل متعارض يذكر أي مصدر حقيقة حسم التعارض");
    var c = corrections.save(new DataCorrection(UUID.fromString(p.facilityId()), p.governorate(), id, r.issueType(), r.description(), r.resolutionBasis(), done));
    return Map.of("ok",true,"correction",c,"corrected",done);
  }
}
