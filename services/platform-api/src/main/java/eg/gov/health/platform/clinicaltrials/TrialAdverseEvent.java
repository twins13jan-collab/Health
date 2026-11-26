package eg.gov.health.platform.clinicaltrials;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "trial_adverse_events")
public class TrialAdverseEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="participant_id", nullable=false) private UUID participantId;
  private String description; private String severity;
  @Column(name="reported_to_ethics", nullable=false) private boolean reportedToEthics = false;
  @Column(name="action_taken") private String actionTaken;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TrialAdverseEvent() {}
  public TrialAdverseEvent(UUID facilityId, String governorate, UUID participantId, String description, String severity, boolean reportedToEthics, String actionTaken) {
    this.facilityId=facilityId; this.governorate=governorate; this.participantId=participantId; this.description=description;
    this.severity=severity; this.reportedToEthics=reportedToEthics; this.actionTaken=actionTaken;
  }
  public UUID getId(){return id;}
}
