package eg.gov.health.platform.telemedicine;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "tele_sessions")
public class TeleSession {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="clinician_ref") private String clinicianRef;
  @Column(name="parties_verified", nullable=false) private boolean partiesVerified = false;
  @Column(name="consent_obtained", nullable=false) private boolean consentObtained = false;
  @Column(name="exam_limitations_documented", nullable=false) private boolean examLimitationsDocumented = false;
  @Column(name="in_person_decision") private String inPersonDecision;
  @Column(name="recording_authorized", nullable=false) private boolean recordingAuthorized = false;
  @Column(nullable=false) private String status = "scheduled";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TeleSession() {}
  public TeleSession(UUID facilityId, String governorate, String patientRef, String clinicianRef, boolean partiesVerified,
                       boolean consentObtained, boolean examLimitationsDocumented, String inPersonDecision, boolean recordingAuthorized) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.clinicianRef=clinicianRef;
    this.partiesVerified=partiesVerified; this.consentObtained=consentObtained; this.examLimitationsDocumented=examLimitationsDocumented;
    this.inPersonDecision=inPersonDecision; this.recordingAuthorized=recordingAuthorized;
  }
  public UUID getId(){return id;} public boolean isPartiesVerified(){return partiesVerified;} public boolean isConsentObtained(){return consentObtained;}
  public boolean isExamLimitationsDocumented(){return examLimitationsDocumented;} public String getInPersonDecision(){return inPersonDecision;}
  public boolean isRecordingAuthorized(){return recordingAuthorized;}
  public void activate(){ this.status="active"; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
