package eg.gov.health.platform.patientsafety;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "prevention_actions")
public class PreventionAction {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="analysis_id", nullable=false) private UUID analysisId;
  private String action;
  @Column(nullable=false) private boolean done = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected PreventionAction() {}
  public PreventionAction(UUID facilityId, String governorate, UUID analysisId, String action, boolean done) {
    this.facilityId=facilityId; this.governorate=governorate; this.analysisId=analysisId; this.action=action; this.done=done;
  }
  public UUID getId(){return id;} public boolean isDone(){return done;}
}
