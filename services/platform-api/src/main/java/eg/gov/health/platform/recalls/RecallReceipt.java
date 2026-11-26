package eg.gov.health.platform.recalls;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "recall_receipts")
public class RecallReceipt {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="notice_id", nullable=false) private UUID noticeId;
  @Column(nullable=false) private String location;
  @Column(name="acknowledged_by", nullable=false) private String acknowledgedBy;
  @Column(name="quarantined_qty", nullable=false) private int quarantinedQty = 0;
  @Column(name="acknowledged_at") private OffsetDateTime acknowledgedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RecallReceipt() {}
  public RecallReceipt(UUID facilityId, String governorate, UUID noticeId, String location, String acknowledgedBy, int quarantinedQty) {
    this.facilityId=facilityId; this.governorate=governorate; this.noticeId=noticeId; this.location=location;
    this.acknowledgedBy=acknowledgedBy; this.quarantinedQty=quarantinedQty;
  }
  public UUID getId(){return id;} public int getQuarantinedQty(){return quarantinedQty;}
}
