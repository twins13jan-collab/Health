package eg.gov.health.platform.recalls;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "drug_batches")
public class DrugBatch {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="item_id", nullable=false) private UUID itemId;
  @Column(name="batch_number", nullable=false) private String batchNumber;
  private String manufacturer; private String supplier;
  @Column(name="production_date") private LocalDate productionDate;
  @Column(name="expiry_date", nullable=false) private LocalDate expiryDate;
  @Column(name="release_status", nullable=false) private String releaseStatus = "released";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DrugBatch() {}
  public DrugBatch(UUID facilityId, String governorate, UUID itemId, String batchNumber, String manufacturer, String supplier, LocalDate productionDate, LocalDate expiryDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.itemId=itemId; this.batchNumber=batchNumber;
    this.manufacturer=manufacturer; this.supplier=supplier; this.productionDate=productionDate; this.expiryDate=expiryDate;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public UUID getItemId(){return itemId;}
  public String getReleaseStatus(){return releaseStatus;} public LocalDate getExpiryDate(){return expiryDate;}
  public void setReleaseStatus(String s){ this.releaseStatus=s; this.updatedAt=OffsetDateTime.now(); }
}
