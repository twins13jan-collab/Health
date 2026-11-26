package eg.gov.health.platform.purchasing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "goods_receipts")
public class GoodsReceipt {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="po_id", nullable=false) private UUID poId;
  @Column(name="received_by", nullable=false) private String receivedBy;
  @Column(name="quantity_received", nullable=false) private int quantityReceived = 0;
  @Column(nullable=false) private boolean partial = false;
  @Column(nullable=false) private boolean substitute = false;
  @Column(name="technical_approval") private String technicalApproval;
  @Column(name="contract_approval") private String contractApproval;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected GoodsReceipt() {}
  public GoodsReceipt(UUID facilityId, String governorate, UUID poId, String receivedBy, int quantityReceived, boolean partial, boolean substitute, String technicalApproval, String contractApproval) {
    this.facilityId=facilityId; this.governorate=governorate; this.poId=poId; this.receivedBy=receivedBy;
    this.quantityReceived=quantityReceived; this.partial=partial; this.substitute=substitute;
    this.technicalApproval=technicalApproval; this.contractApproval=contractApproval;
  }
  public UUID getId(){return id;} public String getReceivedBy(){return receivedBy;} public UUID getPoId(){return poId;}
  public UUID getFacilityId(){return facilityId;}
}
