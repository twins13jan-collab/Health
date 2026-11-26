package eg.gov.health.platform.meddelivery;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "delivery_returns")
public class DeliveryReturn {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="order_id", nullable=false) private UUID orderId;
  private String reason; private String disposition;
  @Column(name="pharmacist_received") private String pharmacistReceived;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DeliveryReturn() {}
  public DeliveryReturn(UUID facilityId, String governorate, UUID orderId, String reason, String disposition, String pharmacistReceived) {
    this.facilityId=facilityId; this.governorate=governorate; this.orderId=orderId; this.reason=reason; this.disposition=disposition; this.pharmacistReceived=pharmacistReceived;
  }
  public UUID getId(){return id;}
}
