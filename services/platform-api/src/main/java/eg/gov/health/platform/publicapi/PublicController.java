package eg.gov.health.platform.publicapi;
import eg.gov.health.platform.dashboard.Aggregates;
import eg.gov.health.platform.facility.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
/** Public live data for the system's homepage: national aggregates,
 *  the facility directory, and capacity by governorate. Anonymous by
 *  design (permitAll + the 'public' RLS read setting) and safe by
 *  construction: only counts and public facility identity/capacity —
 *  no patient-level data exists in these responses. */
@RestController @RequestMapping("/api/public")
public class PublicController {
  private final Aggregates aggregates; private final FacilityRepository facilities;
  private final eg.gov.health.platform.aiadmin.AiRoutingService aiRouting;
  private final com.fasterxml.jackson.databind.ObjectMapper json = new com.fasterxml.jackson.databind.ObjectMapper();
  @org.springframework.beans.factory.annotation.Value("${app.analytics-ai-base-url:http://localhost:8000}")
  private String analyticsBaseUrl;
  private final java.net.http.HttpClient http = java.net.http.HttpClient.newBuilder()
      .version(java.net.http.HttpClient.Version.HTTP_1_1)
      .connectTimeout(java.time.Duration.ofSeconds(4)).build();
  private final eg.gov.health.platform.settings.SettingsService settings;
  private final org.springframework.jdbc.core.JdbcTemplate jdbc;
  public PublicController(Aggregates aggregates, FacilityRepository facilities,
                          eg.gov.health.platform.aiadmin.AiRoutingService aiRouting,
                          eg.gov.health.platform.settings.SettingsService settings,
                          org.springframework.jdbc.core.JdbcTemplate jdbc) {
    this.aggregates = aggregates; this.facilities = facilities; this.aiRouting = aiRouting;
    this.settings = settings; this.jdbc = jdbc;
  }

  /** The complete sign-in / registration hierarchy: ALL 27
   *  governorates (not only those that already have facilities),
   *  each with its health directorate — named after the
   *  governorate — and the platform's complete facility-type
   *  taxonomy. The cascades in /login and /register render from
   *  this, so the full structure is always choosable. */
  @GetMapping("/hierarchy")
  Map<String, Object> hierarchy() {
    var govs = jdbc.queryForList(
        "SELECT g.code, g.name_ar, g.name_en, d.name_ar AS dir_ar, d.name_en AS dir_en "
        + "FROM ref_governorates g LEFT JOIN ref_directorates d ON d.governorate_code = g.code "
        + "ORDER BY g.name_ar").stream().map(r -> {
          Map<String, Object> m = new java.util.LinkedHashMap<>();
          m.put("code", r.get("code")); m.put("nameAr", r.get("name_ar")); m.put("nameEn", r.get("name_en"));
          m.put("directorateAr", r.get("dir_ar")); m.put("directorateEn", r.get("dir_en"));
          return m;
        }).toList();
    var types = eg.gov.health.platform.facility.FacilityTypes.ALL.stream().map(t -> {
      Map<String, Object> m = new java.util.LinkedHashMap<>();
      m.put("value", t.value()); m.put("nameAr", t.nameAr()); m.put("nameEn", t.nameEn());
      return m;
    }).toList();
    return Map.of("governorates", govs, "facilityTypes", types);
  }

  /** Public subset of the platform settings (identity, contact,
   *  maintenance banner, capacity toggle) — the public website
   *  renders from these values, so the super-admin's panel edits
   *  are what the public actually sees. */
  @GetMapping("/settings")
  Map<String, Object> publicSettings() {
    var snap = settings.snapshot();
    Map<String, Object> out = new java.util.LinkedHashMap<>();
    snap.forEach((k, v) -> {
      if (eg.gov.health.platform.settings.SettingsCatalog.isPublic(k)) out.put(k, v);
    });
    return Map.of("settings", out);
  }
  @GetMapping("/summary")
  Map<String, Object> summary() {
    var m = new LinkedHashMap<String, Object>();
    m.put("kpis", aggregates.kpis(Aggregates.Scope.national()));
    var byType = new TreeMap<String, Long>();
    facilities.findAll().stream().filter(f -> "active".equals(f.getStatus()))
      .forEach(f -> byType.merge(f.getType(), 1L, Long::sum));
    m.put("facilitiesByType", byType);
    var byGov = new TreeMap<String, Long>();
    facilities.findAll().stream().filter(f -> "active".equals(f.getStatus()))
      .forEach(f -> byGov.merge(f.getGovernorate() == null ? "—" : f.getGovernorate(), 1L, Long::sum));
    m.put("facilitiesByGovernorate", byGov);
    m.put("generatedAt", OffsetDateTime.now());
    return m;
  }
  @GetMapping("/facilities")
  Map<String, Object> facilities() {
    var rows = aggregates.facilityDirectory(Aggregates.Scope.national());
    return Map.of("facilities", rows, "count", rows.size(), "generatedAt", OffsetDateTime.now());
  }
  @GetMapping("/capacity")
  Map<String, Object> capacity() {
    var rows = aggregates.capacityByGovernorate(Aggregates.Scope.national());
    return Map.of("capacity", rows, "count", rows.size(), "generatedAt", OffsetDateTime.now());
  }

  /** Governorates that actually have active facilities, with
   *  counts — the entry point of the public service finder. */
  @GetMapping("/governorates")
  Map<String, Object> governorates() {
    var byGov = new TreeMap<String, Long>();
    facilities.findAll().stream().filter(f -> "active".equals(f.getStatus()))
      .forEach(f -> byGov.merge(f.getGovernorate() == null ? "—" : f.getGovernorate(), 1L, Long::sum));
    var rows = byGov.entrySet().stream().map(e -> Map.of(
        "governorate", e.getKey(), "facilities", e.getValue())).toList();
    return Map.of("governorates", rows, "count", rows.size());
  }

  private static List<String> servicesOf(String type) {
    return BookableServices.serviceKeysOf(type);
  }

  /** Type filter equivalence: "lab" and its legacy stored synonym
   *  "laboratory" are one type (merged in the taxonomy); every
   *  other type matches exactly. */
  private static boolean typeMatches(String stored, String requested) {
    if (requested.equals(stored)) return true;
    return ("lab".equals(requested) || "laboratory".equals(requested))
        && ("lab".equals(stored) || "laboratory".equals(stored));
  }

  /** The bookable-services catalog itself: every service a citizen
   *  can book or request, its fulfilment mode, and the facility
   *  types that offer it. The finder renders its service filter
   *  from this — one source of truth. */
  @GetMapping("/bookable-services")
  Map<String, Object> bookableServices() {
    var rows = BookableServices.CATALOG.stream().map(b -> {
      Map<String, Object> v = new LinkedHashMap<>();
      v.put("key", b.key()); v.put("nameAr", b.nameAr()); v.put("nameEn", b.nameEn());
      v.put("mode", b.mode()); v.put("portalType", b.portalType());
      v.put("facilityTypes", b.facilityTypes());
      return v;
    }).toList();
    return Map.of("services", rows, "count", rows.size());
  }

  /** Bookable services by governorate, facility type and/or the
   *  service itself: the public finder behind the homepage. Only
   *  active facilities. The governorate matches in any of its
   *  stored forms (code / Arabic / English). */
  @GetMapping("/services")
  Map<String, Object> services(@RequestParam(required = false) String governorate,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String service) {
    var rows = facilities.findAll().stream()
        .filter(f -> "active".equals(f.getStatus()))
        .filter(f -> governorate == null || GovernorateNames.matches(f.getGovernorate(), governorate))
        .filter(f -> type == null || typeMatches(f.getType(), type))
        .filter(f -> service == null || BookableServices.offers(f.getType(), service))
        .map(f -> {
          Map<String, Object> v = new LinkedHashMap<>();
          v.put("id", String.valueOf(f.getId()));
          v.put("nameAr", f.getNameAr());
          v.put("nameEn", f.getNameEn());
          v.put("type", f.getType());
          v.put("level", f.getLevel());
          v.put("governorate", f.getGovernorate());
          v.put("city", f.getCity());
          v.put("bedCapacity", f.getBedCapacity());
          v.put("services", servicesOf(f.getType()));
          return v;
        }).toList();
    return Map.of("facilities", rows, "count", rows.size(), "generatedAt", OffsetDateTime.now());
  }

  /** The homepage chatbot: the same agent as the staff assistant,
   *  but anonymous — its tools are the public ones only (national
   *  summary, facility directory, capacity), so it can answer
   *  about services and availability and never sees patient data.
   *  The active external provider (if any) is injected exactly as
   *  in the authenticated proxy. */
  @PostMapping("/chat")
  org.springframework.http.ResponseEntity<String> chat(@RequestBody String body) {
    String payload = body == null || body.isBlank() ? "{}" : body;
    try {
      var cfg = aiRouting.activeConfig();
      if (cfg.isPresent()) {
        var root = json.readValue(payload, java.util.Map.class);
        var c = cfg.get();
        root.put("provider", Map.of("name", c.name(), "baseUrl", c.baseUrl(), "model", c.model(),
            "apiKey", c.apiKey(), "authStyle", c.authStyle()));
        payload = json.writeValueAsString(root);
      }
    } catch (Exception ignored) { /* fall back to the local model */ }
    try {
      if (analyticsBaseUrl.startsWith("unix://")) {
        var r = eg.gov.health.platform.analyticsproxy.UnixSocketHttp.request(
            analyticsBaseUrl.substring("unix://".length()), "POST", "/v1/ai/chat", payload);
        return org.springframework.http.ResponseEntity.status(r.status())
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(r.body());
      }
      var req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(analyticsBaseUrl + "/v1/ai/chat"))
          .timeout(java.time.Duration.ofSeconds(240))
          .header("Content-Type", "application/json")
          .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload)).build();
      var resp = http.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
      return org.springframework.http.ResponseEntity.status(resp.statusCode())
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(resp.body());
    } catch (Exception e) {
      return org.springframework.http.ResponseEntity.status(502)
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .body("{\"error\":\"analytics_unavailable\",\"message\":\"المساعد غير متاح الآن\"}");
    }
  }
}
