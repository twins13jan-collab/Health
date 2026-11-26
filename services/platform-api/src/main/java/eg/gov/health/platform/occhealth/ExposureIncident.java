package eg.gov.health.platform.occhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "exposure_incidents")
public class ExposureIncident {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="worker_id", nullable=false) private UUID workerId;
  @Column(name="exposure_type") private String exposureType;
  @Column(name="dose_or_level") private String doseOrLevel;
  @Column(name="occurred_at") private OffsetDateTime occurredAt;
  @Column(name="action_taken") private String actionTaken;
  @Column(name="follow_up_exam_done", nullable=false) private boolean followUpExamDone = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ExposureIncident() {}
  public ExposureIncident(UUID facilityId, String governorate, UUID workerId, String exposureType, String doseOrLevel, OffsetDateTime occurredAt, String actionTaken) {
    this.facilityId=facilityId; this.governorate=governorate; this.workerId=workerId; this.exposureType=exposureType;
    this.doseOrLevel=doseOrLevel; this.occurredAt=occurredAt; this.actionTaken=actionTaken;
  }
  public UUID getId(){return id;} public boolean isFollowUpExamDone(){return followUpExamDone;}
  public void completeFollowUp(){ this.followUpExamDone=true; this.updatedAt=OffsetDateTime.now(); }
}
