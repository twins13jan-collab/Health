package eg.gov.health.platform.hta;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "hta_assessments")
public class HtaAssessment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="technology_name") private String technologyName;
  @Column(name="tech_type") private String techType;
  private String requester;
  private String effectiveness; private String safety;
  @Column(name="cost_effectiveness") private String costEffectiveness;
  @Column(name="budget_impact") private String budgetImpact;
  @Column(name="ethical_impact") private String ethicalImpact;
  @Column(name="evidence_quality") private String evidenceQuality;
  private String applicability;
  @Column(name="stakeholders_consulted", nullable=false) private boolean stakeholdersConsulted = false;
  @Column(nullable=false) private String status = "intake";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected HtaAssessment() {}
  public HtaAssessment(UUID facilityId, String governorate, String technologyName, String techType, String requester) {
    this.facilityId=facilityId; this.governorate=governorate; this.technologyName=technologyName; this.techType=techType; this.requester=requester;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getEvidenceQuality(){return evidenceQuality;}
  public String getEffectiveness(){return effectiveness;} public String getSafety(){return safety;} public boolean isStakeholdersConsulted(){return stakeholdersConsulted;}
  public void appraise(String effectiveness, String safety, String costEffectiveness, String budgetImpact, String ethicalImpact,
                       String evidenceQuality, String applicability, boolean stakeholdersConsulted) {
    this.effectiveness=effectiveness; this.safety=safety; this.costEffectiveness=costEffectiveness; this.budgetImpact=budgetImpact;
    this.ethicalImpact=ethicalImpact; this.evidenceQuality=evidenceQuality; this.applicability=applicability;
    this.stakeholdersConsulted=stakeholdersConsulted; this.status="appraisal"; this.updatedAt=OffsetDateTime.now();
  }
  public void decide(){ this.status="decided"; this.updatedAt=OffsetDateTime.now(); }
}
