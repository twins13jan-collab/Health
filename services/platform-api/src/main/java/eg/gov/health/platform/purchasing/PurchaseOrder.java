package eg.gov.health.platform.purchasing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity(name = "ProcPurchaseOrder") @Table(name = "proc_purchase_orders")
public class PurchaseOrder {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="request_id") private UUID requestId;
  @Column(name="tender_id") private UUID tenderId;
  @Column(name="supplier_id", nullable=false) private UUID supplierId;
  private String items;
  @Column(name="total_amount", nullable=false) private java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
  @Column(nullable=false) private java.math.BigDecimal taxes = java.math.BigDecimal.ZERO;
  @Column(name="delivery_terms") private String deliveryTerms;
  private String warranty; @Column(name="issued_by") private String issuedBy;
  @Column(nullable=false) private int version = 1;
  @Column(name="amendment_of") private UUID amendmentOf;
  @Column(nullable=false) private String status = "issued";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PurchaseOrder() {}
  public PurchaseOrder(UUID facilityId, String governorate, UUID requestId, UUID tenderId, UUID supplierId, String items,
                       java.math.BigDecimal totalAmount, java.math.BigDecimal taxes, String deliveryTerms, String warranty,
                       String issuedBy, int version, UUID amendmentOf) {
    this.facilityId=facilityId; this.governorate=governorate; this.requestId=requestId; this.tenderId=tenderId; this.supplierId=supplierId;
    this.items=items; this.totalAmount=totalAmount==null?java.math.BigDecimal.ZERO:totalAmount;
    this.taxes=taxes==null?java.math.BigDecimal.ZERO:taxes; this.deliveryTerms=deliveryTerms; this.warranty=warranty;
    this.issuedBy=issuedBy; this.version=version; this.amendmentOf=amendmentOf;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getIssuedBy(){return issuedBy;}
  public java.math.BigDecimal getTotalAmount(){return totalAmount;} public int getVersion(){return version;} public UUID getSupplierId(){return supplierId;}
  public String getItems(){return items;} public UUID getRequestId(){return requestId;} public UUID getTenderId(){return tenderId;}
  public java.math.BigDecimal getTaxes(){return taxes;} public String getDeliveryTerms(){return deliveryTerms;} public String getWarranty(){return warranty;}
  public void markAmended(){ this.status="amended"; this.updatedAt=OffsetDateTime.now(); }
}
