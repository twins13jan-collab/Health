package eg.gov.health.platform.labsafety;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "test_introductions")
public class TestIntroduction {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="test_name") private String testName;
  private String analyzer;
  @Column(name="risk_assessment_done", nullable=false) private boolean riskAssessmentDone = false;
  @Column(name="accuracy_verified", nullable=false) private boolean accuracyVerified = false;
  @Column(name="sensitivity_verified", nullable=false) private boolean sensitivityVerified = false;
  @Column(name="interferences_checked", nullable=false) private boolean interferencesChecked = false;
  @Column(nullable=false) private boolean approved = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TestIntroduction() {}
  public TestIntroduction(UUID facilityId, String governorate, String testName, String analyzer, boolean risk, boolean accuracy, boolean sensitivity, boolean interferences) {
    this.facilityId=facilityId; this.governorate=governorate; this.testName=testName; this.analyzer=analyzer; this.riskAssessmentDone=risk;
    this.accuracyVerified=accuracy; this.sensitivityVerified=sensitivity; this.interferencesChecked=interferences;
  }
  public UUID getId(){return id;} public boolean isApproved(){return approved;}
  public boolean isRiskAssessmentDone(){return riskAssessmentDone;} public boolean isAccuracyVerified(){return accuracyVerified;}
  public boolean isSensitivityVerified(){return sensitivityVerified;} public boolean isInterferencesChecked(){return interferencesChecked;}
  public void approve(){ this.approved=true; this.updatedAt=OffsetDateTime.now(); }
}
