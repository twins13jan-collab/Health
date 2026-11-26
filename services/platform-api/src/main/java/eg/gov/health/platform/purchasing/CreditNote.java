package eg.gov.health.platform.purchasing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "credit_notes")
public class CreditNote {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="invoice_id", nullable=false) private UUID invoiceId;
  @Column(nullable=false) private java.math.BigDecimal amount = java.math.BigDecimal.ZERO;
  private String reason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CreditNote() {}
  public CreditNote(UUID facilityId, String governorate, UUID invoiceId, java.math.BigDecimal amount, String reason) {
    this.facilityId=facilityId; this.governorate=governorate; this.invoiceId=invoiceId;
    this.amount=amount==null?java.math.BigDecimal.ZERO:amount; this.reason=reason;
  }
  public UUID getId(){return id;}
}
