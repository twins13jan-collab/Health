package eg.gov.health.platform.patientsafety;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "safety_incidents")
public class SafetyIncident {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="incident_type") private String incidentType;
  private String severity;
  @Column(name="reporter_ref") private String reporterRef;
  private String description;
  @Column(name="patient_notified", nullable=false) private boolean patientNotified = false;
  @Column(nullable=false) private String status = "reported";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SafetyIncident() {}
  public SafetyIncident(UUID facilityId, String governorate, String incidentType, String severity, String reporterRef, String description) {
    this.facilityId=facilityId; this.governorate=governorate; this.incidentType=incidentType; this.severity=severity;
    this.reporterRef=reporterRef; this.description=description;
  }
  public UUID getId(){return id;} public String getIncidentType(){return incidentType;} public boolean isPatientNotified(){return patientNotified;}
  public void notifyPatient(){ this.patientNotified=true; this.updatedAt=OffsetDateTime.now(); }
  public void markAnalyzed(){ this.status="analyzed"; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
}
