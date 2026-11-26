package eg.gov.health.platform.settings;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Admin control panel API for ALL platform settings & variables.
 *  Reads return the full catalogue with current values; writes are
 *  validated against SettingsCatalog (unknown keys and ill-typed
 *  values are refused), applied immediately through SettingsService,
 *  and audited. System administrators only. */
@RestController @RequestMapping("/api/admin/settings")
public class SettingsController {
  private final SettingsService settings; private final AuditService audit;
  public SettingsController(SettingsService settings, AuditService audit) {
    this.settings = settings; this.audit = audit;
  }
  private TenantContext.Principal requireAdmin() {
    var p = TenantContext.get();
    if (p == null || !"super_admin".equals(p.role()))
      throw BizException.forbidden("إعدادات ومتغيرات المنظومة للأدمن الموسّع فقط");
    return p;
  }

  @GetMapping
  Map<String,Object> list() {
    requireAdmin();
    var values = settings.snapshot();
    List<Map<String,Object>> rows = new ArrayList<>();
    for (var spec : SettingsCatalog.all()) {
      Map<String,Object> r = new LinkedHashMap<>();
      r.put("key", spec.key()); r.put("type", spec.type()); r.put("group", spec.group());
      r.put("public", spec.isPublic()); r.put("value", values.get(spec.key()));
      rows.add(r);
    }
    return Map.of("settings", rows, "count", rows.size());
  }

  public record ValueBody(String value) {}
  @PutMapping("/{key}")
  Map<String,Object> update(@PathVariable String key, @RequestBody ValueBody r) {
    var p = requireAdmin();
    String error = SettingsCatalog.validate(key, r.value());
    if (error != null) throw BizException.badRequest("setting_invalid", error);
    settings.set(key, r.value().trim(), p.email());
    audit.record("admin.setting_updated", "platform_setting", key, "{}");
    return Map.of("ok", true, "key", key, "value", r.value().trim());
  }
}
