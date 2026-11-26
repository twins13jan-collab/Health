package eg.gov.health.platform.insurancedeep;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "insurance_claims")
public class InsuranceClaim {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="service_ref") private String serviceRef;
  @Column(name="documentation_complete", nullable=false) private boolean documentationComplete = false;
  @Column(name="tariff_match", nullable=false) private boolean tariffMatch = false;
  @Column(name="medical_necessity", nullable=false) private boolean medicalNecessity = false;
  @Column(name="coverage_decision") private String coverageDecision;
  @Column(name="fraud_flag", nullable=false) private boolean fraudFlag = false;
  @Column(name="fraud_human_reviewed", nullable=false) private boolean fraudHumanReviewed = false;
  @Column(nullable=false) private boolean settled = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected InsuranceClaim() {}
  public InsuranceClaim(UUID facilityId, String governorate, String patientRef, String serviceRef, boolean documentationComplete,
                          boolean tariffMatch, boolean medicalNecessity, boolean fraudFlag) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.serviceRef=serviceRef;
    this.documentationComplete=documentationComplete; this.tariffMatch=tariffMatch; this.medicalNecessity=medicalNecessity; this.fraudFlag=fraudFlag;
  }
  public UUID getId(){return id;} public boolean isDocumentationComplete(){return documentationComplete;} public boolean isTariffMatch(){return tariffMatch;}
  public String getPatientRef(){return patientRef;} public String getServiceRef(){return serviceRef;}
  public String getCoverageDecision(){return coverageDecision;} public boolean isSettled(){return settled;}
  public OffsetDateTime getCreatedAt(){return createdAt;} public UUID getFacilityId(){return facilityId;}
  public boolean isMedicalNecessity(){return medicalNecessity;} public boolean isFraudFlag(){return fraudFlag;}
  public boolean isFraudHumanReviewed(){return fraudHumanReviewed;}
  public void decide(String decision){ this.coverageDecision=decision; this.updatedAt=OffsetDateTime.now(); }
  public void reviewFraud(){ this.fraudHumanReviewed=true; this.updatedAt=OffsetDateTime.now(); }
  public void settle(){ this.settled=true; this.updatedAt=OffsetDateTime.now(); }
}
