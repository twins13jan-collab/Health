package eg.gov.health.platform.nutrition;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "nutrition_plans")
public class NutritionPlan {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="assessment_id") private UUID assessmentId;
  @Column(name="estimated_needs_kcal") private Integer estimatedNeedsKcal;
  @Column(nullable=false) private String route;
  private String goal; private String restrictions;
  @Column(name="proposed_by") private String proposedBy;
  @Column(name="approved_by") private String approvedBy;
  @Column(nullable=false) private String status = "proposed";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected NutritionPlan() {}
  public NutritionPlan(UUID facilityId, String governorate, UUID patientId, UUID assessmentId, Integer estimatedNeedsKcal,
                       String route, String goal, String restrictions, String proposedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.assessmentId=assessmentId;
    this.estimatedNeedsKcal=estimatedNeedsKcal; this.route=route; this.goal=goal; this.restrictions=restrictions; this.proposedBy=proposedBy;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public UUID getPatientId(){return patientId;}
  public String getStatus(){return status;} public Integer getEstimatedNeedsKcal(){return estimatedNeedsKcal;}
  public void approve(String by){ this.approvedBy=by; this.status="approved"; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
