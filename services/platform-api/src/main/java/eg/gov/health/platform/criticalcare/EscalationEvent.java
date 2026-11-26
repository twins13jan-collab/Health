package eg.gov.health.platform.criticalcare;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "escalation_events")
public class EscalationEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="stay_id", nullable=false) private UUID stayId;
  @Column(nullable=false) private String reason;
  @Column(name="team_called", nullable=false) private String teamCalled;
  @Column(name="called_at") private OffsetDateTime calledAt = OffsetDateTime.now();
  @Column(name="responded_at") private OffsetDateTime respondedAt;
  private String decision;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected EscalationEvent() {}
  public EscalationEvent(UUID facilityId, String governorate, UUID stayId, String reason, String teamCalled) {
    this.facilityId=facilityId; this.governorate=governorate; this.stayId=stayId; this.reason=reason; this.teamCalled=teamCalled;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public OffsetDateTime getCalledAt(){return calledAt;}
  public String getReason(){return reason;} public String getTeamCalled(){return teamCalled;}
  public void respond(String decision){ this.decision=decision; this.respondedAt=OffsetDateTime.now(); this.status="responded"; this.updatedAt=OffsetDateTime.now(); }
}
