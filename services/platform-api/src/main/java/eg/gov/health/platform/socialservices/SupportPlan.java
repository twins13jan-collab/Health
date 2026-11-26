package eg.gov.health.platform.socialservices;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "support_plans")
public class SupportPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="assessment_id", nullable=false) private UUID assessmentId;
  private String actions;
  @Column(name="follow_up_date") private LocalDate followUpDate;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SupportPlan() {}
  public SupportPlan(UUID facilityId, String governorate, UUID assessmentId, String actions, LocalDate followUpDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.assessmentId=assessmentId; this.actions=actions; this.followUpDate=followUpDate;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getAssessmentId(){return assessmentId;}
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
