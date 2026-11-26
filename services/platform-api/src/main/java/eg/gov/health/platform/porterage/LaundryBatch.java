package eg.gov.health.platform.porterage;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "laundry_batches")
public class LaundryBatch {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="batch_ref") private String batchRef;
  @Column(name="batch_type") private String batchType;
  @Column(name="sent_count") private Integer sentCount;
  @Column(name="returned_count") private Integer returnedCount;
  @Column(nullable=false) private String status = "sent";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected LaundryBatch() {}
  public LaundryBatch(UUID facilityId, String governorate, String batchRef, String batchType, Integer sentCount) {
    this.facilityId=facilityId; this.governorate=governorate; this.batchRef=batchRef; this.batchType=batchType; this.sentCount=sentCount;
  }
  public UUID getId(){return id;} public Integer getSentCount(){return sentCount;}
  public String getBatchRef(){return batchRef;} public String getBatchType(){return batchType;} public Integer getReturnedCount(){return returnedCount;}
  public String getStatus(){return status;} public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;}
  public void receive(int returned){ this.returnedCount=returned; this.status="returned"; this.updatedAt=OffsetDateTime.now(); }
}
