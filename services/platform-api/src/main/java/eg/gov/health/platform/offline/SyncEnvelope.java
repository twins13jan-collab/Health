package eg.gov.health.platform.offline;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "sync_envelopes")
public class SyncEnvelope {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="package_id", nullable=false) private UUID packageId;
  @Column(name="sync_id", nullable=false) private String syncId;
  @Column(name="content_hash") private String contentHash;
  @Column(name="event_count", nullable=false) private int eventCount = 0;
  @Column(name="applied_count", nullable=false) private int appliedCount = 0;
  @Column(nullable=false) private String status = "received";
  @Column(name="received_at") private OffsetDateTime receivedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SyncEnvelope() {}
  public SyncEnvelope(UUID facilityId, String governorate, UUID packageId, String syncId, String contentHash, int eventCount) {
    this.facilityId=facilityId; this.governorate=governorate; this.packageId=packageId; this.syncId=syncId;
    this.contentHash=contentHash; this.eventCount=eventCount;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public int getAppliedCount(){return appliedCount;}
  public void finish(String status, int applied){ this.status=status; this.appliedCount=applied; }
}
