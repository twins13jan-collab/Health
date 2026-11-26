package eg.gov.health.platform.quality;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "complaints")
public class Complaint {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id") private UUID patientId;
  @Column(name="complainant_name") private String complainantName;
  @Column(nullable=false) private String category;
  @Column(nullable=false) private String severity = "low"; // low, medium, high, critical
  @Column(nullable=false) private String description;
  @Column(nullable=false) private String status = "received"; // received, investigating, resolved, closed, escalated
  @Column(name="resolution") private String resolution;
  @Column(name="due_at") private OffsetDateTime dueAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Complaint() {}
  public Complaint(UUID facilityId, String governorate, UUID patientId, String complainantName, String category, String severity, String description) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.complainantName=complainantName;
    this.category=category; this.severity=severity==null?"low":severity; this.description=description;
    this.dueAt = ComplaintSla.dueAt(this.createdAt, this.severity);
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public OffsetDateTime getDueAt(){return dueAt;}
  public OffsetDateTime getCreatedAt(){return createdAt;} public String getSeverity(){return severity;}
  public void resolve(String res){this.resolution=res; this.status="resolved"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
