package eg.gov.health.platform.purchasing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "purchase_requests")
public class PurchaseRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(nullable=false) private String requester;
  @Column(name="item_description", nullable=false) private String itemDescription;
  private String specs; @Column(nullable=false) private int quantity = 1;
  private String justification; @Column(name="approved_by") private String approvedBy;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected PurchaseRequest() {}
  public PurchaseRequest(UUID facilityId, String governorate, String requester, String itemDescription, String specs, int quantity, String justification) {
    this.facilityId=facilityId; this.governorate=governorate; this.requester=requester; this.itemDescription=itemDescription;
    this.specs=specs; this.quantity=quantity; this.justification=justification;
  }
  public UUID getId(){return id;} public String getRequester(){return requester;} public String getStatus(){return status;}
  public void decide(String status, String by){ this.status=status; this.approvedBy=by; this.updatedAt=OffsetDateTime.now(); }
}
