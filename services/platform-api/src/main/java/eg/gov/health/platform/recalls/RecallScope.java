package eg.gov.health.platform.recalls;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "recall_scopes")
public class RecallScope {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="notice_id", nullable=false) private UUID noticeId;
  @Column(name="batch_id", nullable=false) private UUID batchId;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RecallScope() {}
  public RecallScope(UUID facilityId, String governorate, UUID noticeId, UUID batchId) {
    this.facilityId=facilityId; this.governorate=governorate; this.noticeId=noticeId; this.batchId=batchId;
  }
  public UUID getId(){return id;} public UUID getBatchId(){return batchId;}
}
