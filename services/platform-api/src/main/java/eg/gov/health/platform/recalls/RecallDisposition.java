package eg.gov.health.platform.recalls;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "recall_dispositions")
public class RecallDisposition {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="notice_id", nullable=false) private UUID noticeId;
  @Column(name="batch_id", nullable=false) private UUID batchId;
  @Column(nullable=false) private String disposition;
  @Column(nullable=false) private int quantity;
  @Column(name="approved_by", nullable=false) private String approvedBy;
  @Column(name="executed_at") private OffsetDateTime executedAt = OffsetDateTime.now();
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected RecallDisposition() {}
  public RecallDisposition(UUID facilityId, String governorate, UUID noticeId, UUID batchId, String disposition, int quantity, String approvedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.noticeId=noticeId; this.batchId=batchId;
    this.disposition=disposition; this.quantity=quantity; this.approvedBy=approvedBy;
  }
  public UUID getId(){return id;} public int getQuantity(){return quantity;}
}
