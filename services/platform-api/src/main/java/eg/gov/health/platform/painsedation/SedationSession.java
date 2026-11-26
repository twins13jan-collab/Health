package eg.gov.health.platform.painsedation;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "sedation_sessions")
public class SedationSession {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="procedure_name") private String procedureName;
  @Column(name="pre_assessment_done", nullable=false) private boolean preAssessmentDone = false;
  @Column(name="airway_assessed", nullable=false) private boolean airwayAssessed = false;
  @Column(name="consent_documented", nullable=false) private boolean consentDocumented = false;
  @Column(name="risk_class") private String riskClass;
  @Column(name="aldrete_score") private Integer aldreteScore;
  private String complications;
  @Column(nullable=false) private String status = "planned";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SedationSession() {}
  public SedationSession(UUID facilityId, String governorate, String patientRef, String procedureName, boolean preAssessmentDone,
                         boolean airwayAssessed, boolean consentDocumented, String riskClass) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientRef=patientRef; this.procedureName=procedureName;
    this.preAssessmentDone=preAssessmentDone; this.airwayAssessed=airwayAssessed; this.consentDocumented=consentDocumented; this.riskClass=riskClass;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public boolean isPreAssessmentDone(){return preAssessmentDone;} public boolean isAirwayAssessed(){return airwayAssessed;} public boolean isConsentDocumented(){return consentDocumented;}
  public Integer getAldreteScore(){return aldreteScore;}
  public void start(){ this.status="in_progress"; this.updatedAt=OffsetDateTime.now(); }
  public void toRecovery(Integer aldrete, String complications){ this.aldreteScore=aldrete; this.complications=complications;
    this.status = complications != null && !complications.isBlank() ? "complicated" : "recovery"; this.updatedAt=OffsetDateTime.now(); }
  public void discharge(){ this.status="discharged"; this.updatedAt=OffsetDateTime.now(); }
}
