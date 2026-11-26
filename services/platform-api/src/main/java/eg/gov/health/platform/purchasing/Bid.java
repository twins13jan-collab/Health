package eg.gov.health.platform.purchasing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "bids")
public class Bid {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="tender_id", nullable=false) private UUID tenderId;
  @Column(name="supplier_id", nullable=false) private UUID supplierId;
  @Column(nullable=false) private java.math.BigDecimal amount = java.math.BigDecimal.ZERO;
  private java.math.BigDecimal score; @Column(name="scored_by") private String scoredBy;
  @Column(nullable=false) private String status = "submitted";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected Bid() {}
  public Bid(UUID facilityId, String governorate, UUID tenderId, UUID supplierId, java.math.BigDecimal amount) {
    this.facilityId=facilityId; this.governorate=governorate; this.tenderId=tenderId; this.supplierId=supplierId;
    this.amount=amount==null?java.math.BigDecimal.ZERO:amount;
  }
  public UUID getId(){return id;} public java.math.BigDecimal getAmount(){return amount;} public UUID getSupplierId(){return supplierId;}
  public String getStatus(){return status;}
  public void score(java.math.BigDecimal s, String by){ this.score=s; this.scoredBy=by; this.status="evaluated"; }
  public void setStatus(String s){ this.status=s; }
}
