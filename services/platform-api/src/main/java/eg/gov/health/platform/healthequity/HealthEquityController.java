package eg.gov.health.platform.healthequity;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Health equity & service access (module 74). The distribution
 *  of services and resources is measured against population,
 *  need and disease burden; access gaps are analysed by location,
 *  age, gender, disability and social factors with privacy
 *  protected; real travel time, access cost, transport and
 *  support services are measured; coverage programmes serve
 *  remote and deprived areas with mobile clinics; and
 *  non-discrimination in access is followed up. */
@RestController @RequestMapping("/api/health-equity")
public class HealthEquityController {
  private final EquityMeasurementRepository measurements; private final AccessGapAnalysisRepository gaps;
  private final CoverageProgramRepository programs; private final AuditService audit;
  public HealthEquityController(EquityMeasurementRepository measurements, AccessGapAnalysisRepository gaps,
                                CoverageProgramRepository programs, AuditService audit) {
    this.measurements=measurements; this.gaps=gaps; this.programs=programs; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record MeasureBody(String region, Integer population, Integer serviceUnits, Double diseaseBurdenIndex) {}
  @PostMapping("/measurements")
  Map<String,Object> measure(@RequestBody MeasureBody r) {
    var p = principal();
    var m = measurements.save(new EquityMeasurement(UUID.fromString(p.facilityId()), p.governorate(), r.region(), r.population(), r.serviceUnits(), r.diseaseBurdenIndex()));
    double adjusted = EquityRules.needAdjustedUnitsPer10k(r.serviceUnits(), r.population(), r.diseaseBurdenIndex());
    return Map.of("ok",true,"measurement",m,"need_adjusted_units_per_10k",adjusted,"underserved",EquityRules.underserved(adjusted));
  }
  public record GapBody(String region, String dimension, Double gapPct, Integer sampleSize) {}
  @PostMapping("/gap-analyses")
  Map<String,Object> gap(@RequestBody GapBody r) {
    var p = principal();
    var g = gaps.save(new AccessGapAnalysis(UUID.fromString(p.facilityId()), p.governorate(), r.region(), r.dimension(), r.gapPct(), r.sampleSize()));
    return Map.of("ok",true,"analysis",g,"publishable",EquityRules.gapPublishable(r.sampleSize()));
  }
  @PostMapping("/gap-analyses/{id}/publish")
  Map<String,Object> publish(@PathVariable UUID id) {
    principal();
    var g = gaps.findById(id).orElseThrow(() -> BizException.notFound("analysis_not_found","التحليل غير موجود"));
    if (!EquityRules.gapPublishable(g.getSampleSize()))
      throw BizException.conflict("small_sample","تحليل فجوات بعينة دون 30 لا يُنشر — المجموعات الصغيرة تُحجب حماية للخصوصية لا تُعرَض");
    g.publish(); gaps.save(g);
    audit.record("equity.gap.published","access_gap_analysis",id.toString(),"{}");
    return Map.of("ok",true,"analysis",g,"published",true);
  }
  public record ProgramBody(String region, String programType, Double targetGapPct) {}
  @PostMapping("/coverage-programs")
  Map<String,Object> program(@RequestBody ProgramBody r) {
    var p = principal();
    var pr = programs.save(new CoverageProgram(UUID.fromString(p.facilityId()), p.governorate(), r.region(), r.programType(), r.targetGapPct()));
    return Map.of("ok",true,"program",pr);
  }
  public record GapMeasureBody(Double measuredGapPct) {}
  @PostMapping("/coverage-programs/{id}/measure-gap")
  Map<String,Object> measureGap(@PathVariable UUID id, @RequestBody GapMeasureBody r) {
    principal();
    var pr = programs.findById(id).orElseThrow(() -> BizException.notFound("program_not_found","البرنامج غير موجود"));
    pr.recordGap(r.measuredGapPct()); programs.save(pr);
    return Map.of("ok",true,"program",pr);
  }
  @PostMapping("/coverage-programs/{id}/close")
  Map<String,Object> closeProgram(@PathVariable UUID id) {
    principal();
    var pr = programs.findById(id).orElseThrow(() -> BizException.notFound("program_not_found","البرنامج غير موجود"));
    if (!EquityRules.programClosable(pr.getLatestMeasuredGapPct(), pr.getTargetGapPct()))
      throw BizException.conflict("gap_above_target","برنامج تغطية لا يُغلق قبل قياس جديد يُظهر الفجوة عند هدفه أو دونه");
    pr.close(); programs.save(pr);
    return Map.of("ok",true,"program",pr);
  }
}
