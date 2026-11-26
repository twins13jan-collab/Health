package eg.gov.health.platform.aiadmin;
import eg.gov.health.platform.common.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/** Control panel for the external AI provider group — system
 *  administrators only (checked here on every method, like account
 *  administration). API keys are write-only: accepted over the
 *  authenticated admin session, stored AES-GCM-encrypted, and only
 *  ever read back masked. Every change is audited. */
@RestController @RequestMapping("/api/admin/ai")
public class AiSettingsController {
  private final AiProviderSettingsRepository repo;
  private final AuditService audit;
  private final SecretKeySpec key;
  private final HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
      .connectTimeout(Duration.ofSeconds(5)).build();

  public AiSettingsController(AiProviderSettingsRepository repo, AuditService audit,
                              @Value("${app.settings-key:}") String settingsKeyBase64) {
    this.repo = repo; this.audit = audit;
    this.key = SecretBox.keyFromBase64(settingsKeyBase64);
  }

  private TenantContext.Principal requireAdmin() {
    var p = TenantContext.get();
    if (p == null || !"super_admin".equals(p.role()))
      throw BizException.forbidden("إعدادات الذكاء الاصطناعي من إعدادات المنظومة — للأدمن الموسّع فقط");
    return p;
  }

  private Map<String, Object> view(AiProviderSettings s) {
    var preset = AiProviders.PRESETS.get(s.getProvider());
    Map<String, Object> v = new LinkedHashMap<>();
    v.put("provider", s.getProvider());
    v.put("displayAr", preset == null ? s.getProvider() : preset.displayAr());
    v.put("displayEn", preset == null ? s.getProvider() : preset.displayEn());
    v.put("baseUrl", preset == null ? null : preset.baseUrl());
    v.put("enabled", s.isEnabled());
    v.put("active", s.isActive());
    v.put("model", s.getModel());
    v.put("defaultModel", preset == null ? null : preset.defaultModel());
    String masked = null;
    if (s.getApiKeyEnc() != null && key != null) {
      try { masked = SecretBox.mask(SecretBox.decrypt(key, s.getApiKeyEnc())); } catch (Exception ignored) { masked = "•••• (غير قابل للقراءة)"; }
    }
    v.put("keyMasked", masked);
    v.put("hasKey", s.getApiKeyEnc() != null);
    v.put("updatedBy", s.getUpdatedBy());
    v.put("updatedAt", s.getUpdatedAt());
    return v;
  }

  @GetMapping("/providers")
  Map<String, Object> list() {
    requireAdmin();
    var rows = repo.findAll().stream()
        .sorted(Comparator.comparing(AiProviderSettings::getProvider)).map(this::view).toList();
    return Map.of("providers", rows, "encryptionReady", key != null,
        "localModel", System.getenv().getOrDefault("AI_MODEL", "qwen2.5:3b"));
  }

  public record UpdateRequest(Boolean enabled, Boolean makeActive, String model, String apiKey, Boolean clearKey) {}
  @PutMapping("/providers/{provider}")
  Map<String, Object> update(@PathVariable String provider, @RequestBody UpdateRequest r) {
    var p = requireAdmin();
    if (!AiProviders.PRESETS.containsKey(provider))
      throw BizException.notFound("provider_unknown", "مزود غير معروف: " + provider);
    var s = repo.findById(provider).orElseThrow(() -> BizException.notFound("provider_missing", "إعدادات المزود غير موجودة"));
    if (r.apiKey() != null && !r.apiKey().isBlank()) {
      if (key == null) throw BizException.badRequest("encryption_not_configured",
          "مفتاح تشفير الخادم (APP_SETTINGS_KEY) غير مهيأ — لا تُحفظ مفاتيح دون تشفير");
      s.setApiKeyEnc(SecretBox.encrypt(key, r.apiKey().trim()));
    }
    if (Boolean.TRUE.equals(r.clearKey())) s.setApiKeyEnc(null);
    if (r.model() != null) s.setModel(r.model().isBlank() ? null : r.model().trim());
    if (r.enabled() != null) s.setEnabled(r.enabled());
    if (Boolean.TRUE.equals(r.makeActive())) {
      repo.findAll().forEach(o -> { if (o.isActive() && !o.getProvider().equals(provider)) { o.setActive(false); repo.save(o); } });
      s.setActive(true);
      s.setEnabled(true);
    }
    s.setUpdatedBy(p.email());
    repo.save(s);
    audit.record("ai.provider_updated", "ai_provider", provider,
        "{\"active\":" + s.isActive() + ",\"enabled\":" + s.isEnabled() + ",\"keyChanged\":" + (r.apiKey() != null && !r.apiKey().isBlank()) + "}");
    return view(s);
  }

  /** Live key check: asks the provider for its model list with the
   *  stored key. Reports only ok/status — never the key, and a
   *  provider error is reported as-is, not hidden. */
  @PostMapping("/providers/{provider}/test")
  Map<String, Object> test(@PathVariable String provider) {
    requireAdmin();
    var preset = AiProviders.PRESETS.get(provider);
    if (preset == null) throw BizException.notFound("provider_unknown", "مزود غير معروف: " + provider);
    var s = repo.findById(provider).orElseThrow(() -> BizException.notFound("provider_missing", "إعدادات المزود غير موجودة"));
    if (s.getApiKeyEnc() == null) return Map.of("ok", false, "reason", "no_key", "message", "لا يوجد مفتاح محفوظ لهذا المزود بعد");
    if (key == null) return Map.of("ok", false, "reason", "encryption_not_configured", "message", "مفتاح تشفير الخادم غير مهيأ");
    String apiKey = SecretBox.decrypt(key, s.getApiKeyEnc());
    try {
      var b = HttpRequest.newBuilder(URI.create(preset.baseUrl() + "/models")).timeout(Duration.ofSeconds(12)).GET();
      if ("x-api-key".equals(preset.authStyle())) b.header("x-api-key", apiKey); else b.header("Authorization", "Bearer " + apiKey);
      var resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
      boolean ok = resp.statusCode() >= 200 && resp.statusCode() < 300;
      return Map.of("ok", ok, "status", resp.statusCode(),
          "message", ok ? "المفتاح صالح والمزود يستجيب" : "المزود رفض الطلب — راجع المفتاح أو رصيد الحساب");
    } catch (Exception e) {
      return Map.of("ok", false, "reason", "unreachable", "message", "تعذر الوصول إلى المزود من الخادم");
    }
  }
}
