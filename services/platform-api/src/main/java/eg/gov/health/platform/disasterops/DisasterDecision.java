package eg.gov.health.platform.disasterops;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "disaster_decision_log")
public class DisasterDecision {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="event_id", nullable=false) private UUID eventId;
  private String decision;
  @Column(name="decided_by") private String decidedBy;
  @Column(name="authority_basis") private String authorityBasis;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DisasterDecision() {}
  public DisasterDecision(UUID facilityId, String governorate, UUID eventId, String decision, String decidedBy, String authorityBasis) {
    this.facilityId=facilityId; this.governorate=governorate; this.eventId=eventId; this.decision=decision; this.decidedBy=decidedBy; this.authorityBasis=authorityBasis;
  }
  public UUID getId(){return id;}
}
