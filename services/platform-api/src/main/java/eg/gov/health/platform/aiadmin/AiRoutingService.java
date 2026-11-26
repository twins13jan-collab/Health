package eg.gov.health.platform.aiadmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.spec.SecretKeySpec;
import java.util.Optional;

/** Resolves the currently active external AI configuration for the
 *  chat proxy: the provider the administrator activated in the
 *  control panel, when it is enabled and holds a key. The decrypted
 *  key is handed only to the analytics service over the internal
 *  network, inside the proxied chat request — browsers never see
 *  it, and it is never logged. */
@Service
public class AiRoutingService {
  public record ActiveConfig(String name, String baseUrl, String model, String apiKey, String authStyle) {}
  private final AiProviderSettingsRepository repo;
  private final SecretKeySpec key;

  public AiRoutingService(AiProviderSettingsRepository repo,
                          @Value("${app.settings-key:}") String settingsKeyBase64) {
    this.repo = repo;
    this.key = SecretBox.keyFromBase64(settingsKeyBase64);
  }

  public boolean encryptionReady() { return key != null; }

  public Optional<ActiveConfig> activeConfig() {
    if (key == null) return Optional.empty();
    return repo.findByActiveTrue()
        .filter(AiProviderSettings::isEnabled)
        .filter(s -> s.getApiKeyEnc() != null && !s.getApiKeyEnc().isBlank())
        .map(s -> {
          var preset = AiProviders.PRESETS.get(s.getProvider());
          if (preset == null) return null;
          String model = s.getModel() == null || s.getModel().isBlank() ? preset.defaultModel() : s.getModel();
          return new ActiveConfig(s.getProvider(), preset.baseUrl(), model,
              SecretBox.decrypt(key, s.getApiKeyEnc()), preset.authStyle());
        });
  }
}
