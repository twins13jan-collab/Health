package eg.gov.health.platform.operations;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "purchase_orders")
public class PurchaseOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(nullable=false) private String supplier;
  @Column(name="item_code", nullable=false) private String itemCode;
  @Column(nullable=false) private int quantity;
  @Column(name="unit_price", nullable=false) private BigDecimal unitPrice;
  @Column(nullable=false) private String status = "draft"; // draft, approved, ordered, partially_received, received, cancelled
  @Column(name="approved_by") private String approvedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PurchaseOrder() {}
  public PurchaseOrder(UUID facilityId, String governorate, String supplier, String itemCode, int quantity, BigDecimal unitPrice) {
    this.facilityId=facilityId; this.governorate=governorate; this.supplier=supplier; this.itemCode=itemCode;
    this.quantity=quantity; this.unitPrice=unitPrice;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getItemCode(){return itemCode;}
  public int getQuantity(){return quantity;} public UUID getFacilityId(){return facilityId;}
  public BigDecimal total(){ return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
  public void approve(String by){this.approvedBy=by; this.status="approved"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
