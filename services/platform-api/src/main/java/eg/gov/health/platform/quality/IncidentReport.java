package eg.gov.health.platform.quality;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "incident_reports")
public class IncidentReport {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id") private UUID patientId;
  @Column(nullable=false) private String category; // fall, medication, pressure_ulcer, infection, equipment, other
  @Column(nullable=false) private String severity; // near_miss, minor, moderate, major, catastrophic
  @Column(nullable=false) private String description;
  @Column(name="reported_by") private String reportedBy;
  @Column(nullable=false) private String status = "reported"; // reported, under_review, action_assigned, closed
  @Column(name="root_cause") private String rootCause;
  @Column(name="corrective_action") private String correctiveAction;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected IncidentReport() {}
  public IncidentReport(UUID facilityId, String governorate, UUID patientId, String category, String severity, String description, String reportedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.category=category;
    this.severity=severity; this.description=description; this.reportedBy=reportedBy;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public String getSeverity(){return severity;}
  public void review(String rootCause, String action){ this.rootCause=rootCause; this.correctiveAction=action; this.status="action_assigned"; this.updatedAt=OffsetDateTime.now(); }
  public void close(){ this.status="closed"; this.updatedAt=OffsetDateTime.now(); }
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
