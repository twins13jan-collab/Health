package eg.gov.health.platform.followup;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "followup_tasks")
public class FollowUpTask {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="plan_id", nullable=false) private UUID planId;
  private String kind; @Column(name="due_at") private OffsetDateTime dueAt;
  @Column(name="completed_at") private OffsetDateTime completedAt;
  private String outcome;
  @Column(name="contact_attempts", nullable=false) private int contactAttempts = 0;
  @Column(nullable=false) private String status = "pending";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected FollowUpTask() {}
  public FollowUpTask(UUID facilityId, String governorate, UUID planId, String kind, OffsetDateTime dueAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.planId=planId; this.kind=kind; this.dueAt=dueAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public OffsetDateTime getDueAt(){return dueAt;} public int getContactAttempts(){return contactAttempts;}
  public void complete(String outcome){ this.outcome=outcome; this.completedAt=OffsetDateTime.now(); this.status="done"; this.updatedAt=OffsetDateTime.now(); }
  public void attempt(){ this.contactAttempts++; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
