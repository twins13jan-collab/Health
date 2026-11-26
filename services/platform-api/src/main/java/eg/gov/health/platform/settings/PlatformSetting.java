package eg.gov.health.platform.settings;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity @Table(name = "platform_settings")
public class PlatformSetting {
  @Id @Column(name = "setting_key") private String key;
  @Column(name = "setting_value") private String value;
  @Column(name = "updated_by") private String updatedBy;
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PlatformSetting() {}
  public PlatformSetting(String key, String value, String updatedBy) {
    this.key = key; this.value = value; this.updatedBy = updatedBy;
  }
  public String getKey() { return key; } public String getValue() { return value; }
  public String getUpdatedBy() { return updatedBy; } public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public void setValue(String v, String by) { this.value = v; this.updatedBy = by; this.updatedAt = OffsetDateTime.now(); }
}
