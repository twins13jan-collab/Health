package eg.gov.health.platform.organbank;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "donation_cases")
public class DonationCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="donor_code", nullable=false) private String donorCode;
  @Column(name="donation_type") private String donationType;
  @Column(name="organ_or_tissue") private String organOrTissue;
  @Column(name="consent_basis") private String consentBasis;
  @Column(name="consent_documented", nullable=false) private boolean consentDocumented = false;
  @Column(name="coercion_screened", nullable=false) private boolean coercionScreened = false;
  @Column(name="incentive_flag", nullable=false) private boolean incentiveFlag = false;
  @Column(name="medical_evaluation") private String medicalEvaluation;
  @Column(name="evaluation_complete", nullable=false) private boolean evaluationComplete = false;
  @Column(name="committee_decision") private String committeeDecision;
  @Column(nullable=false) private String status = "evaluation";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DonationCase() {}
  public DonationCase(UUID facilityId, String governorate, String donorCode, String donationType, String organOrTissue, String consentBasis,
                      boolean consentDocumented, boolean coercionScreened, boolean incentiveFlag, String medicalEvaluation, boolean evaluationComplete) {
    this.facilityId=facilityId; this.governorate=governorate; this.donorCode=donorCode; this.donationType=donationType; this.organOrTissue=organOrTissue;
    this.consentBasis=consentBasis; this.consentDocumented=consentDocumented; this.coercionScreened=coercionScreened; this.incentiveFlag=incentiveFlag;
    this.medicalEvaluation=medicalEvaluation; this.evaluationComplete=evaluationComplete;
    if (incentiveFlag) this.status="blocked";
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public boolean isConsentDocumented(){return consentDocumented;} public boolean isEvaluationComplete(){return evaluationComplete;}
  public boolean isCoercionScreened(){return coercionScreened;} public boolean isIncentiveFlag(){return incentiveFlag;}
  public String getOrganOrTissue(){return organOrTissue;} public String getCommitteeDecision(){return committeeDecision;}
  public void committee(String decision){ this.committeeDecision=decision; if ("approved".equals(decision)) this.status="approved"; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
