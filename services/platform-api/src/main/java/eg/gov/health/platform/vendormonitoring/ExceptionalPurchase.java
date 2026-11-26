package eg.gov.health.platform.vendormonitoring;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "exceptional_purchases")
public class ExceptionalPurchase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="item_ref") private String itemRef;
  @Column(name="vendor_ref") private String vendorRef;
  private String reason;
  @Column(nullable=false) private boolean documented = false;
  @Column(nullable=false) private boolean reviewed = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ExceptionalPurchase() {}
  public ExceptionalPurchase(UUID facilityId, String governorate, String itemRef, String vendorRef, String reason, boolean documented) {
    this.facilityId=facilityId; this.governorate=governorate; this.itemRef=itemRef; this.vendorRef=vendorRef; this.reason=reason; this.documented=documented;
  }
  public UUID getId(){return id;} public boolean isDocumented(){return documented;} public boolean isReviewed(){return reviewed;}
  public void review(){ this.reviewed=true; this.updatedAt=OffsetDateTime.now(); }
}
