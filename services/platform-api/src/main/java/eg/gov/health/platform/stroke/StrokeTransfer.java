package eg.gov.health.platform.stroke;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "stroke_transfers")
public class StrokeTransfer {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="activation_id", nullable=false) private UUID activationId;
  @Column(name="destination_center") private String destinationCenter;
  @Column(name="acceptance_documented", nullable=false) private boolean acceptanceDocumented = false;
  @Column(name="accepted_by") private String acceptedBy;
  @Column(name="departed_at") private OffsetDateTime departedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected StrokeTransfer() {}
  public StrokeTransfer(UUID facilityId, String governorate, UUID activationId, String destinationCenter, boolean acceptanceDocumented, String acceptedBy, OffsetDateTime departedAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.activationId=activationId; this.destinationCenter=destinationCenter;
    this.acceptanceDocumented=acceptanceDocumented; this.acceptedBy=acceptedBy; this.departedAt=departedAt;
  }
  public UUID getId(){return id;}
}
