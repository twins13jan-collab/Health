package eg.gov.health.platform.refdata;
import eg.gov.health.platform.common.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** National reference data (completion batch 5): governorates,
 *  the ICD-10 structure and starter code set, and WHO
 *  Essential Medicines with ATC codes — served as governed
 *  reference for every module, searchable, and extended only
 *  through the terminology/reference-data governance modules. */
@RestController @RequestMapping("/api/ref-data")
public class RefDataController {
  private final JdbcTemplate jdbc;
  public RefDataController(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  private void principal() {
    if (TenantContext.get() == null) throw BizException.forbidden("يتطلب تسجيل الدخول");
  }
  @GetMapping("/governorates")
  @Cacheable("governorates")
  public Map<String,Object> governorates() {
    principal();
    return Map.of("ok",true,"governorates",jdbc.queryForList(
      "SELECT g.code, g.name_ar, g.name_en, g.region_id, r.name_ar AS region_ar, r.name_en AS region_en "
      + "FROM ref_governorates g LEFT JOIN ref_regions r ON r.id = g.region_id ORDER BY g.name_en"));
  }
  @GetMapping("/regions")
  @Cacheable("regions")
  public Map<String,Object> regions() {
    principal();
    return Map.of("ok",true,"regions",jdbc.queryForList(
      "SELECT id, name_ar, name_en FROM ref_regions ORDER BY id"));
  }
  /** Cities/districts of one governorate, by its code (CAI, GIZ…). */
  @GetMapping("/cities")
  @Cacheable(value = "cities", key = "#governorate")
  public Map<String,Object> cities(@RequestParam String governorate) {
    principal();
    var rows = jdbc.queryForList(
      "SELECT id, name_ar, name_en FROM ref_cities WHERE governorate_code = ? ORDER BY name_ar", governorate);
    return Map.of("ok",true,"cities",rows,"count",rows.size(),"governorate",governorate);
  }
  @GetMapping("/icd10/chapters")
  Map<String,Object> chapters() {
    principal();
    return Map.of("ok",true,"chapters",jdbc.queryForList("SELECT chapter, range_start, range_end, title_ar, title_en FROM ref_icd10_chapters ORDER BY range_start"));
  }
  @GetMapping("/icd10")
  Map<String,Object> icd10(@RequestParam(defaultValue="") String search) {
    principal();
    var rows = search.isBlank()
      ? jdbc.queryForList("SELECT code, title_ar, title_en, chapter FROM ref_icd10_codes ORDER BY code LIMIT 500")
      : jdbc.queryForList("SELECT code, title_ar, title_en, chapter FROM ref_icd10_codes WHERE code ILIKE ? OR title_ar ILIKE ? OR title_en ILIKE ? ORDER BY code LIMIT 200",
          "%"+search+"%", "%"+search+"%", "%"+search+"%");
    return Map.of("ok",true,"codes",rows,"count",rows.size());
  }
  @GetMapping("/medicines")
  Map<String,Object> medicines(@RequestParam(defaultValue="") String search) {
    principal();
    var rows = search.isBlank()
      ? jdbc.queryForList("SELECT atc_code, name_ar, name_en, form FROM ref_essential_medicines ORDER BY atc_code LIMIT 500")
      : jdbc.queryForList("SELECT atc_code, name_ar, name_en, form FROM ref_essential_medicines WHERE atc_code ILIKE ? OR name_ar ILIKE ? OR name_en ILIKE ? ORDER BY atc_code LIMIT 200",
          "%"+search+"%", "%"+search+"%", "%"+search+"%");
    return Map.of("ok",true,"medicines",rows,"count",rows.size());
  }
  @GetMapping("/summary")
  Map<String,Object> summary() {
    principal();
    return Map.of("ok",true,
      "governorates", jdbc.queryForObject("SELECT count(*) FROM ref_governorates", Long.class),
      "regions", jdbc.queryForObject("SELECT count(*) FROM ref_regions", Long.class),
      "cities", jdbc.queryForObject("SELECT count(*) FROM ref_cities", Long.class),
      "icd10_chapters", jdbc.queryForObject("SELECT count(*) FROM ref_icd10_chapters", Long.class),
      "icd10_codes", jdbc.queryForObject("SELECT count(*) FROM ref_icd10_codes", Long.class),
      "essential_medicines", jdbc.queryForObject("SELECT count(*) FROM ref_essential_medicines", Long.class),
      "scope","starter national set — the full official ICD-10 release and national formulary load through terminology governance (modules 137/182/235), versioned");
  }
}
