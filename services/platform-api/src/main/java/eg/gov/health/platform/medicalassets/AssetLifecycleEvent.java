package eg.gov.health.platform.medicalassets;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "asset_lifecycle_events")
public class AssetLifecycleEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="asset_id", nullable=false) private UUID assetId;
  @Column(name="event_type", nullable=false) private String eventType;
  private String details; private String actor;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AssetLifecycleEvent() {}
  public AssetLifecycleEvent(UUID facilityId, String governorate, UUID assetId, String eventType, String details, String actor) {
    this.facilityId=facilityId; this.governorate=governorate; this.assetId=assetId; this.eventType=eventType; this.details=details; this.actor=actor;
  }
  public UUID getId(){return id;}
}
