package eg.gov.health.platform.strategy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** The National Health Strategy 2024–2030 inside the platform:
 *  its vision, mission, guiding principles, seven priorities and
 *  44 goals (reference data, V108) — public for transparency
 *  (priority 5) and community awareness (priority 7) — plus, for
 *  signed-in leadership, a monitoring board that measures each
 *  priority from the platform's own live operational data
 *  (priority 5, goal 6: strong digital M&E systems). Indicators
 *  are computed only from data the platform truly holds; a
 *  measure that cannot be computed shows as unavailable, never
 *  as an invented number. */
@RestController
public class StrategyController {
  private final JdbcTemplate jdbc;
  public StrategyController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public static final String VISION_AR = "أن ينعم جميع المقيمين على أرض مصر بحياة صحية آمنة، بما يعزز ازدهار وإنتاجية المجتمع";
  public static final String MISSION_AR = "تعزيز صحة ورفاهية جميع المقيمين على أرض مصر من خلال التشجيع على نمط حياة صحي، ومعالجة المحددات الرئيسية للصحة، وتقوية النظم الصحية";
  public static final List<String> PRINCIPLES_AR = List.of(
      "العدالة", "الشفافية", "الكرامة", "السرية", "الملكية", "الشراكة", "الشمولية", "التفاعل والمشاركة", "المساءلة");

  private List<Map<String,Object>> priorities() {
    var rows = jdbc.queryForList(
        "SELECT code, seq, title_ar, title_en FROM ref_strategy_priorities ORDER BY seq");
    var goals = jdbc.queryForList(
        "SELECT priority_code, seq, title_ar FROM ref_strategy_goals ORDER BY priority_code, seq");
    Map<String, List<Map<String,Object>>> byPriority = new LinkedHashMap<>();
    for (var g : goals) byPriority.computeIfAbsent(String.valueOf(g.get("priority_code")), k -> new ArrayList<>())
        .add(Map.of("seq", g.get("seq"), "titleAr", g.get("title_ar")));
    List<Map<String,Object>> out = new ArrayList<>();
    for (var r : rows) {
      var m = new LinkedHashMap<String,Object>();
      m.put("code", r.get("code")); m.put("seq", r.get("seq"));
      m.put("titleAr", r.get("title_ar")); m.put("titleEn", r.get("title_en"));
      m.put("goals", byPriority.getOrDefault(String.valueOf(r.get("code")), List.of()));
      m.put("capabilities", StrategyAlignment.CAPABILITIES.getOrDefault(String.valueOf(r.get("code")), List.of())
          .stream().map(c -> Map.of("labelAr", c.labelAr(), "labelEn", c.labelEn(), "path", c.path())).toList());
      out.add(m);
    }
    return out;
  }

  @GetMapping("/api/public/strategy")
  Map<String,Object> publicStrategy() {
    return Map.of("ok", true, "vision", VISION_AR, "mission", MISSION_AR,
        "principles", PRINCIPLES_AR, "priorities", priorities(),
        "source", "الاستراتيجية الوطنية للصحة — جمهورية مصر العربية 2024–2030 (وزارة الصحة والسكان ومنظمة الصحة العالمية)");
  }

  private Object measure(String sql) {
    try {
      var v = jdbc.queryForObject(sql, Number.class);
      return v == null ? null : (v.doubleValue() == Math.rint(v.doubleValue()) ? (Object) v.longValue() : Math.round(v.doubleValue() * 10.0) / 10.0);
    } catch (Exception e) { return null; }
  }

  @GetMapping("/api/strategy/board")
  Map<String,Object> board() {
    Map<String, String> sql = new LinkedHashMap<>();
    sql.put("facilities_active", "SELECT count(*) FROM facilities WHERE status='active'");
    sql.put("governorates_covered", "SELECT count(DISTINCT governorate) FROM facilities WHERE status='active'");
    sql.put("phc_facilities", "SELECT count(*) FROM facilities WHERE status='active' AND type IN ('health_unit','family_center','health_center')");
    sql.put("private_facilities", "SELECT count(*) FROM facilities WHERE ownership='private'");
    sql.put("claims_settled_pct", "SELECT CASE WHEN count(*)=0 THEN NULL ELSE 100.0*sum(CASE WHEN settled THEN 1 ELSE 0 END)/count(*) END FROM insurance_claims");
    sql.put("patient_share_pct", "SELECT CASE WHEN sum(total_amount)=0 OR sum(total_amount) IS NULL THEN NULL ELSE 100.0*sum(patient_amount)/sum(total_amount) END FROM invoices WHERE status NOT IN ('draft','cancelled')");
    sql.put("vaccinations_count", "SELECT count(*) FROM vaccinations");
    sql.put("neonatal_screenings_count", "SELECT count(*) FROM neonatal_screenings");
    sql.put("clinic_visits_count", "SELECT count(*) FROM clinic_visits");
    sql.put("patients_registered", "SELECT count(*) FROM patients");
    sql.put("encounters_total", "SELECT count(*) FROM encounters");
    sql.put("encounters_today", "SELECT count(*) FROM encounters WHERE arrived_at::date = CURRENT_DATE");
    sql.put("ambulances_available", "SELECT count(*) FROM ambulance_units WHERE status='available'");
    sql.put("er_open_now", "SELECT count(*) FROM encounters WHERE type='emergency' AND status='open'");
    sql.put("disaster_events_count", "SELECT count(*) FROM disaster_events");
    sql.put("audit_events_count", "SELECT count(*) FROM audit_logs");
    sql.put("matrix_domains", "SELECT count(*) FROM role_permissions");
    sql.put("facilities_licensed_pct", "SELECT CASE WHEN count(*)=0 THEN NULL ELSE 100.0*sum(CASE WHEN license_expiry >= CURRENT_DATE THEN 1 ELSE 0 END)/count(*) END FROM facilities WHERE license_no IS NOT NULL");
    sql.put("portal_patients", "SELECT count(*) FROM app_users WHERE role='patient'");
    sql.put("portal_requests", "SELECT count(*) FROM portal_service_requests");
    sql.put("clinic_sessions_today", "SELECT count(*) FROM clinic_sessions WHERE session_date = CURRENT_DATE");
    sql.put("complaints_total", "SELECT count(*) FROM patient_complaints");
    sql.put("complaints_resolved_pct", "SELECT CASE WHEN count(*)=0 THEN NULL ELSE 100.0*sum(CASE WHEN status IN ('resolved','closed') THEN 1 ELSE 0 END)/count(*) END FROM patient_complaints");
    Map<String,Object> values = new HashMap<>();
    sql.forEach((k, q) -> values.put(k, measure(q)));
    List<Map<String,Object>> out = new ArrayList<>();
    for (var p : priorities()) {
      var m = new LinkedHashMap<String,Object>(p);
      String code = String.valueOf(p.get("code"));
      var inds = StrategyAlignment.INDICATORS.getOrDefault(code, List.of()).stream().map(i -> {
        var im = new LinkedHashMap<String,Object>();
        im.put("key", i.key()); im.put("labelAr", i.labelAr()); im.put("labelEn", i.labelEn());
        im.put("unit", i.unit()); im.put("value", values.get(i.key()));
        return im;
      }).toList();
      m.put("indicators", inds);
      out.add(m);
    }
    return Map.of("ok", true, "priorities", out,
        "note", "مؤشرات تشغيلية حية من بيانات المنظومة نفسها — المستهدفات الوطنية الرسمية كما في الخطة التنفيذية للاستراتيجية، ولا تُستبدل بأرقام تشغيلية");
  }
}
