package eg.gov.health.platform.purchasing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "supplier_invoices")
public class SupplierInvoice {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="po_id", nullable=false) private UUID poId;
  @Column(name="receipt_id") private UUID receiptId;
  @Column(nullable=false) private java.math.BigDecimal amount = java.math.BigDecimal.ZERO;
  @Column(name="held_reason") private String heldReason;
  @Column(name="paid_by") private String paidBy;
  @Column(nullable=false) private String status = "received";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SupplierInvoice() {}
  public SupplierInvoice(UUID facilityId, String governorate, UUID poId, UUID receiptId, java.math.BigDecimal amount) {
    this.facilityId=facilityId; this.governorate=governorate; this.poId=poId; this.receiptId=receiptId;
    this.amount=amount==null?java.math.BigDecimal.ZERO:amount;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public UUID getPoId(){return poId;} public UUID getFacilityId(){return facilityId;}
  public void hold(String reason){ this.status="held"; this.heldReason=reason; this.updatedAt=OffsetDateTime.now(); }
  public void match(){ this.status="matched"; this.updatedAt=OffsetDateTime.now(); }
  public void pay(String by){ this.status="paid"; this.paidBy=by; this.updatedAt=OffsetDateTime.now(); }
}
