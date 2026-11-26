package eg.gov.health.platform.meddelivery;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "med_delivery_attempts")
public class MedDeliveryAttempt {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="order_id", nullable=false) private UUID orderId;
  @Column(name="attempt_no") private Integer attemptNo;
  private String outcome; private String reason;
  @Column(name="temperature_ok") private Boolean temperatureOk;
  @Column(name="received_by") private String receivedBy;
  @Column(name="identity_verified", nullable=false) private boolean identityVerified = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected MedDeliveryAttempt() {}
  public MedDeliveryAttempt(UUID facilityId, String governorate, UUID orderId, int attemptNo, String outcome, String reason,
                         Boolean temperatureOk, String receivedBy, boolean identityVerified) {
    this.facilityId=facilityId; this.governorate=governorate; this.orderId=orderId; this.attemptNo=attemptNo; this.outcome=outcome;
    this.reason=reason; this.temperatureOk=temperatureOk; this.receivedBy=receivedBy; this.identityVerified=identityVerified;
  }
  public UUID getId(){return id;}
}
