package eg.gov.health.platform.radiologydeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "imaging_critical_results")
public class ImagingCriticalResult {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="order_id", nullable=false) private UUID orderId;
  private String finding;
  @Column(name="acknowledged_by") private String acknowledgedBy;
  @Column(nullable=false) private boolean acknowledged = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ImagingCriticalResult() {}
  public ImagingCriticalResult(UUID facilityId, String governorate, UUID orderId, String finding) {
    this.facilityId=facilityId; this.governorate=governorate; this.orderId=orderId; this.finding=finding;
  }
  public UUID getId(){return id;} public boolean isAcknowledged(){return acknowledged;}
  public UUID getOrderId(){return orderId;} public String getFinding(){return finding;}
  public String getAcknowledgedBy(){return acknowledgedBy;} public OffsetDateTime getCreatedAt(){return createdAt;}
  public void acknowledge(String by){ this.acknowledged=true; this.acknowledgedBy=by; }
}
