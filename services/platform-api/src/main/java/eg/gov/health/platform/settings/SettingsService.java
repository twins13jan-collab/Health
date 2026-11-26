package eg.gov.health.platform.settings;
import org.springframework.stereotype.Service;
import java.util.*; import java.util.concurrent.*;
/** Runtime access to platform settings with a short-lived cache
 *  (30s), so panel edits take effect across the platform without a
 *  restart and without a database hit on every request. Reads fall
 *  back to the caller's default when a key is absent or the store is
 *  unreachable — a settings outage must never take the platform
 *  down. */
@Service
public class SettingsService {
  private final PlatformSettingRepository repo;
  private final Map<String,String> cache = new ConcurrentHashMap<>();
  private volatile long loadedAt = 0;
  public SettingsService(PlatformSettingRepository repo) { this.repo = repo; }
  private Map<String,String> all() {
    long now = System.currentTimeMillis();
    if (now - loadedAt > 30_000 || cache.isEmpty()) {
      try {
        Map<String,String> fresh = new ConcurrentHashMap<>();
        for (var s : repo.findAll()) if (s.getValue() != null) fresh.put(s.getKey(), s.getValue());
        cache.clear(); cache.putAll(fresh); loadedAt = now;
      } catch (Exception ignored) { /* keep last known values */ }
    }
    return cache;
  }
  public String get(String key, String def) { return all().getOrDefault(key, def); }
  public int getInt(String key, int def) {
    try { return Integer.parseInt(get(key, String.valueOf(def)).trim()); }
    catch (Exception e) { return def; }
  }
  public boolean getBool(String key, boolean def) {
    String v = get(key, null);
    return v == null ? def : v.equals("true");
  }
  public Map<String,String> snapshot() { return new LinkedHashMap<>(all()); }
  public void set(String key, String value, String by) {
    var existing = repo.findById(key);
    if (existing.isPresent()) { var s = existing.get(); s.setValue(value, by); repo.save(s); }
    else repo.save(new PlatformSetting(key, value, by));
    cache.put(key, value); loadedAt = System.currentTimeMillis();
  }
}
