package eg.gov.health.platform.amrdeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Antimicrobial resistance (module 167). Culture and
 *  susceptibility data are aggregated; organism, antibiotic
 *  and test-method definitions are unified; resistance is
 *  analyzed by facility, region and specimen; consumption is
 *  linked to resistance outcomes; unusual patterns are
 *  verified; and identity-protected surveillance reports are
 *  issued. */
@RestController @RequestMapping("/api/amr-deep")
public class AmrDeepController {
  private final CultureResultRepository cultures; private final AmrSurveillanceReportRepository reports; private final AuditService audit;
  public AmrDeepController(CultureResultRepository cultures, AmrSurveillanceReportRepository reports, AuditService audit) {
    this.cultures=cultures; this.reports=reports; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record CultureBody(String organism, String antibiotic, String result, String specimen, Boolean methodStandardized) {}
  @PostMapping("/cultures")
  Map<String,Object> culture(@RequestBody CultureBody r) {
    var p = principal();
    var c = cultures.save(new CultureResult(UUID.fromString(p.facilityId()), p.governorate(), r.organism(), r.antibiotic(), r.result(),
      r.specimen(), Boolean.TRUE.equals(r.methodStandardized())));
    return Map.of("ok",true,"culture",c,"counts_in_rates",c.isMethodStandardized());
  }
  @GetMapping("/resistance-rate")
  Map<String,Object> rate(@RequestParam String organism, @RequestParam String antibiotic) {
    principal();
    var all = cultures.findByOrganismAndAntibiotic(organism, antibiotic);
    var std = all.stream().filter(CultureResult::isMethodStandardized).toList();
    long resistant = std.stream().filter(c -> "R".equals(c.getResult())).count();
    double rate = AmrRules.resistanceRate(resistant, std.size());
    return Map.of("ok",true,"organism",organism,"antibiotic",antibiotic,
      "resistance_rate_percent",rate,"standardized_results",std.size(),"excluded_non_standardized",all.size()-std.size(),
      "note","rates are computed from standardized-method results only — mixing methods manufactures trends");
  }
  public record ReportBody(String organism, String antibiotic, Double baselineRate, Boolean identityProtected) {}
  @PostMapping("/reports")
  Map<String,Object> report(@RequestBody ReportBody r) {
    var p = principal();
    var all = cultures.findByOrganismAndAntibiotic(r.organism(), r.antibiotic());
    var std = all.stream().filter(CultureResult::isMethodStandardized).toList();
    long resistant = std.stream().filter(c -> "R".equals(c.getResult())).count();
    double rate = AmrRules.resistanceRate(resistant, std.size());
    var rep = reports.save(new AmrSurveillanceReport(UUID.fromString(p.facilityId()), p.governorate(), r.organism(), r.antibiotic(),
      rate, r.baselineRate(), Boolean.TRUE.equals(r.identityProtected())));
    boolean unusual = AmrRules.unusualPattern(rate, r.baselineRate());
    return Map.of("ok",true,"report",rep,"unusual_pattern",unusual);
  }
  @PostMapping("/reports/{id}/verify-pattern")
  Map<String,Object> verify(@PathVariable UUID id) {
    principal();
    var rep = reports.findById(id).orElseThrow(() -> BizException.notFound("report_not_found","التقرير غير موجود"));
    rep.verifyPattern(); reports.save(rep);
    return Map.of("ok",true,"report",rep);
  }
  @PostMapping("/reports/{id}/publish")
  Map<String,Object> publish(@PathVariable UUID id) {
    principal();
    var rep = reports.findById(id).orElseThrow(() -> BizException.notFound("report_not_found","التقرير غير موجود"));
    boolean unusual = AmrRules.unusualPattern(rep.getResistanceRate(), rep.getBaselineRate());
    if (!AmrRules.publishable(rep.isIdentityProtected(), unusual, rep.isPatternVerified()))
      throw BizException.conflict("not_publishable","تقرير ترصد لا يُنشر إلا محمي الهوية — ونمط غير معتاد يقفز فوق خط الأساس يحتاج تحققًا قبل الإعلان: نمط مصنع يُعلن وباءً يهدر موارد وثقة حقيقية");
    rep.publish(); reports.save(rep);
    audit.record("amr.report.published","amr_surveillance_report",id.toString(),"{}");
    return Map.of("ok",true,"report",rep,"published",true);
  }
}
