package eg.gov.health.platform.medicalassets;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "work_orders")
public class WorkOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="asset_id", nullable=false) private UUID assetId;
  @Column(nullable=false) private String type;
  private String technician; @Column(name="spare_parts") private String spareParts;
  @Column(name="spare_part_shortage", nullable=false) private boolean sparePartShortage = false;
  @Column(name="spare_purchase_ref") private String sparePurchaseRef;
  @Column(name="downtime_hours") private java.math.BigDecimal downtimeHours;
  private String result; @Column(name="released_by") private String releasedBy;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected WorkOrder() {}
  public WorkOrder(UUID facilityId, String governorate, UUID assetId, String type, String technician) {
    this.facilityId=facilityId; this.governorate=governorate; this.assetId=assetId; this.type=type; this.technician=technician;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getAssetId(){return assetId;}
  public String getType(){return type;} public String getTechnician(){return technician;}
  public boolean isSparePartShortage(){return sparePartShortage;} public String getSparePurchaseRef(){return sparePurchaseRef;}
  public java.math.BigDecimal getDowntimeHours(){return downtimeHours;} public String getResult(){return result;}
  public OffsetDateTime getCreatedAt(){return createdAt;}
  public void complete(String spareParts, boolean shortage, String purchaseRef, java.math.BigDecimal downtime, String result) {
    this.spareParts=spareParts; this.sparePartShortage=shortage; this.sparePurchaseRef=purchaseRef;
    this.downtimeHours=downtime; this.result=result; this.status="completed"; this.updatedAt=OffsetDateTime.now();
  }
  public void release(String by){ this.releasedBy=by; this.status="released"; this.updatedAt=OffsetDateTime.now(); }
}
