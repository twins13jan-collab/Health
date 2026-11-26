package eg.gov.health.platform.nursing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "nursing_tasks")
public class NursingTask {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="care_plan_id") private UUID carePlanId;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="task_type") private String taskType;
  @Column(name="assigned_to") private String assignedTo;
  @Column(name="due_at") private OffsetDateTime dueAt;
  @Column(name="completed_at") private OffsetDateTime completedAt;
  @Column(nullable=false) private String status = "assigned";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected NursingTask() {}
  public NursingTask(UUID facilityId, String governorate, UUID carePlanId, String patientRef, String taskType, String assignedTo, OffsetDateTime dueAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.carePlanId=carePlanId; this.patientRef=patientRef;
    this.taskType=taskType; this.assignedTo=assignedTo; this.dueAt=dueAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public OffsetDateTime getDueAt(){return dueAt;}
  public void complete(){ this.status="done"; this.completedAt=OffsetDateTime.now(); this.updatedAt=OffsetDateTime.now(); }
  public void reassign(String to){ this.assignedTo=to; this.status="reassigned"; this.updatedAt=OffsetDateTime.now(); }
}
