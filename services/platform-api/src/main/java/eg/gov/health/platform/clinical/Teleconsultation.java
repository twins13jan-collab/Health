package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "teleconsultations")
public class Teleconsultation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id") private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="consultant_name", nullable=false) private String consultantName;
  @Column(name="specialty") private String specialty;
  @Column(name="scheduled_at", nullable=false) private OffsetDateTime scheduledAt;
  @Column(nullable=false) private String status = "scheduled"; // scheduled, in_session, completed, no_show, cancelled, escalated_to_in_person
  @Column(name="session_notes") private String sessionNotes;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Teleconsultation() {}
  public Teleconsultation(UUID facilityId, String governorate, UUID patientId, String consultantName, String specialty, OffsetDateTime scheduledAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.consultantName=consultantName;
    this.specialty=specialty; this.scheduledAt=scheduledAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public void complete(String notes){this.sessionNotes=notes; this.status="completed"; this.updatedAt=OffsetDateTime.now();}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
}
