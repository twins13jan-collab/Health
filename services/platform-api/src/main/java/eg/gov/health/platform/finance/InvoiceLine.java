package eg.gov.health.platform.finance;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "invoice_lines")
public class InvoiceLine {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="invoice_id", nullable=false) private UUID invoiceId;
  @Column(nullable=false) private String description;
  @Column(name="unit_price", nullable=false) private BigDecimal unitPrice;
  @Column(nullable=false) private int quantity = 1;
  @Column(name="coverage_pct", nullable=false) private BigDecimal coveragePct = BigDecimal.ZERO;
  @Column(nullable=false) private String status = "active";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected InvoiceLine() {}
  public InvoiceLine(UUID facilityId, String governorate, UUID invoiceId, String description, BigDecimal unitPrice, int quantity, BigDecimal coveragePct) {
    this.facilityId=facilityId; this.governorate=governorate; this.invoiceId=invoiceId; this.description=description;
    this.unitPrice=unitPrice; this.quantity=quantity; this.coveragePct=coveragePct==null?BigDecimal.ZERO:coveragePct;
  }
  public UUID getId(){return id;} public UUID getInvoiceId()
  {return invoiceId;}
  public BillingCalculator.Line toLine(){ return new BillingCalculator.Line(unitPrice, quantity, coveragePct); }

  public String getDescription(){return description;} public java.math.BigDecimal getUnitPrice(){return unitPrice;} public int getQuantity(){return quantity;} public java.math.BigDecimal getCoveragePct(){return coveragePct;}
}
