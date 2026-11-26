package eg.gov.health.platform.clinical;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "appointments")
public class Appointment {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="patient_id", nullable=false) private UUID patientId;
  @Column(name="clinic") private String clinic;
  @Column(name="doctor_name") private String doctorName;
  @Column(name="starts_at", nullable=false) private OffsetDateTime startsAt;
  @Column(name="ends_at", nullable=false) private OffsetDateTime endsAt;
  @Column(nullable=false) private String status = "booked"; // booked, confirmed, attended, no_show, cancelled, rescheduled
  @Column(name="session_id") private UUID sessionId;
  @Column(name="cancel_reason") private String cancelReason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Appointment() {}
  public Appointment(UUID facilityId, String governorate, UUID patientId, String clinic, String doctorName, OffsetDateTime startsAt, OffsetDateTime endsAt) {
    this.facilityId=facilityId; this.governorate=governorate; this.patientId=patientId; this.clinic=clinic;
    this.doctorName=doctorName; this.startsAt=startsAt; this.endsAt=endsAt;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public UUID getPatientId(){return patientId;} public String getClinic(){return clinic;}
  public UUID getSessionId(){return sessionId;} public void setSessionId(UUID s){this.sessionId=s;}
  public String getCancelReason(){return cancelReason;} public void setCancelReason(String r){this.cancelReason=r;}
  public OffsetDateTime getStartsAt(){return startsAt;} public OffsetDateTime getEndsAt(){return endsAt;}
  public String getDoctorName(){return doctorName;} public UUID getFacilityId(){return facilityId;}
  public void setStatus(String s){this.status=s; this.updatedAt=OffsetDateTime.now();}
  public boolean overlaps(Appointment o) {
    return startsAt.isBefore(o.endsAt) && o.startsAt.isBefore(endsAt) && !"cancelled".equals(o.status);
  }
}
