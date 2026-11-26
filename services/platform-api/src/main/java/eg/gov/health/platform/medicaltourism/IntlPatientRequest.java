package eg.gov.health.platform.medicaltourism;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "intl_patient_requests")
public class IntlPatientRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  private String country;
  @Column(name="file_translated", nullable=false) private boolean fileTranslated = false;
  @Column(name="consent_obtained", nullable=false) private boolean consentObtained = false;
  @Column(name="financial_estimate_approved", nullable=false) private boolean financialEstimateApproved = false;
  @Column(nullable=false) private String status = "received";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected IntlPatientRequest() {}
  public IntlPatientRequest(UUID facilityId, String governorate, String patientRef, String country, boolean fileTranslated, boolean consentObtained, boolean financialEstimateApproved) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.country=country; this.fileTranslated=fileTranslated;
    this.consentObtained=consentObtained; this.financialEstimateApproved=financialEstimateApproved;
  }
  public UUID getId(){return id;} public boolean isFileTranslated(){return fileTranslated;} public boolean isConsentObtained(){return consentObtained;}
  public boolean isFinancialEstimateApproved(){return financialEstimateApproved;}
  public void schedule(){ this.status="scheduled"; this.updatedAt=OffsetDateTime.now(); }
}
