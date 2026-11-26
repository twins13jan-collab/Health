package eg.gov.health.platform.clinicaltrials;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "trial_visits")
public class TrialVisit {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="participant_id", nullable=false) private UUID participantId;
  @Column(name="visit_label") private String visitLabel;
  @Column(name="scheduled_at") private OffsetDateTime scheduledAt;
  @Column(name="completed_at") private OffsetDateTime completedAt;
  @Column(name="samples_ref") private String samplesRef;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TrialVisit() {}
  public TrialVisit(UUID facilityId, String governorate, UUID participantId, String visitLabel, OffsetDateTime scheduledAt, String samplesRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.participantId=participantId; this.visitLabel=visitLabel;
    this.scheduledAt=scheduledAt; this.completedAt=OffsetDateTime.now(); this.samplesRef=samplesRef;
  }
  public UUID getId(){return id;}
}
