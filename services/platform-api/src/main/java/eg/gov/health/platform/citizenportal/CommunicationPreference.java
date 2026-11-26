package eg.gov.health.platform.citizenportal;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "communication_preferences")
public class CommunicationPreference {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="account_id", nullable=false) private UUID accountId;
  private String channel; private String language;
  @Column(name="accessible_format") private String accessibleFormat;
  @Column(name="clinical_alerts", nullable=false) private boolean clinicalAlerts = false;
  @Column(nullable=false) private boolean enabled = true;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CommunicationPreference() {}
  public CommunicationPreference(UUID facilityId, String governorate, UUID accountId, String channel, String language, String accessibleFormat, boolean clinicalAlerts, boolean enabled) {
    this.facilityId=facilityId; this.governorate=governorate; this.accountId=accountId; this.channel=channel; this.language=language;
    this.accessibleFormat=accessibleFormat; this.clinicalAlerts=clinicalAlerts; this.enabled=enabled;
  }
  public UUID getId(){return id;} public boolean isEnabled(){return enabled;} public boolean isClinicalAlerts(){return clinicalAlerts;}
}
