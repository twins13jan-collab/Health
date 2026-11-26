package eg.gov.health.platform.decisionregistry;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "decision_amendments")
public class DecisionAmendment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="decision_id", nullable=false) private UUID decisionId;
  private String action; private String reason; private String actor;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DecisionAmendment() {}
  public DecisionAmendment(UUID facilityId, String governorate, UUID decisionId, String action, String reason, String actor) {
    this.facilityId=facilityId; this.governorate=governorate; this.decisionId=decisionId; this.action=action; this.reason=reason; this.actor=actor;
  }
  public UUID getId(){return id;}
}
