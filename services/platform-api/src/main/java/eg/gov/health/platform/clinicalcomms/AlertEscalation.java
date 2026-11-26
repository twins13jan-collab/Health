package eg.gov.health.platform.clinicalcomms;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "alert_escalations")
public class AlertEscalation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="alert_id", nullable=false) private UUID alertId;
  @Column(name="escalated_to") private String escalatedTo;
  private String reason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AlertEscalation() {}
  public AlertEscalation(UUID facilityId, String governorate, UUID alertId, String escalatedTo, String reason) {
    this.facilityId=facilityId; this.governorate=governorate; this.alertId=alertId; this.escalatedTo=escalatedTo; this.reason=reason;
  }
  public UUID getId(){return id;}
}
