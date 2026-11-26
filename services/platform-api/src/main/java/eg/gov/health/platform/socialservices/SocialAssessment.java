package eg.gov.health.platform.socialservices;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "social_assessments")
public class SocialAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="support_needs") private String supportNeeds;
  @Column(name="risk_level") private String riskLevel;
  @Column(name="safeguarding_concern") private String safeguardingConcern;
  @Column(name="safety_plan_created", nullable=false) private boolean safetyPlanCreated = false;
  @Column(name="assessed_by") private String assessedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SocialAssessment() {}
  public SocialAssessment(UUID facilityId, String governorate, String patientRef, String supportNeeds, String riskLevel,
                          String safeguardingConcern, String assessedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.supportNeeds=supportNeeds;
    this.riskLevel=riskLevel; this.safeguardingConcern=safeguardingConcern; this.assessedBy=assessedBy;
  }
  public UUID getId(){return id;} public String getRiskLevel(){return riskLevel;} public String getSafeguardingConcern(){return safeguardingConcern;}
  public boolean isSafetyPlanCreated(){return safetyPlanCreated;}
  public void createSafetyPlan(){ this.safetyPlanCreated=true; this.updatedAt=OffsetDateTime.now(); }
}
