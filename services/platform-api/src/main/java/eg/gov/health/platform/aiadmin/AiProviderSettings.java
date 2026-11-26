package eg.gov.health.platform.aiadmin;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity @Table(name = "ai_provider_settings")
public class AiProviderSettings {
  @Id private String provider;
  @Column(nullable = false) private boolean enabled = false;
  @Column(name = "is_active", nullable = false) private boolean active = false;
  private String model;
  @Column(name = "api_key_enc") private String apiKeyEnc;
  @Column(name = "updated_by") private String updatedBy;
  @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AiProviderSettings() {}
  public String getProvider() { return provider; }
  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean e) { this.enabled = e; touch(); }
  public boolean isActive() { return active; }
  public void setActive(boolean a) { this.active = a; touch(); }
  public String getModel() { return model; }
  public void setModel(String m) { this.model = m; touch(); }
  public String getApiKeyEnc() { return apiKeyEnc; }
  public void setApiKeyEnc(String k) { this.apiKeyEnc = k; touch(); }
  public String getUpdatedBy() { return updatedBy; }
  public void setUpdatedBy(String u) { this.updatedBy = u; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  private void touch() { this.updatedAt = OffsetDateTime.now(); }
}
