package eg.gov.health.platform.clinics;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "appointment_waitlist")
public class ClinicWaitlistEntry {
  @Id @GeneratedValue private UUID id;
  @Column(name = "session_id", nullable = false) private UUID sessionId;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "patient_id", nullable = false) private UUID patientId;
  private String reason;
  @Column(nullable = false) private String priority = "normal"; // normal, urgent
  @Column(nullable = false) private String status = "waiting";  // waiting, promoted, cancelled
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ClinicWaitlistEntry() {}
  public ClinicWaitlistEntry(UUID sessionId, UUID facilityId, String governorate, UUID patientId, String reason, String priority) {
    this.sessionId = sessionId; this.facilityId = facilityId; this.governorate = governorate;
    this.patientId = patientId; this.reason = reason;
    this.priority = "urgent".equals(priority) ? "urgent" : "normal";
  }
  public UUID getId() { return id; } public UUID getSessionId() { return sessionId; }
  public UUID getFacilityId() { return facilityId; } public UUID getPatientId() { return patientId; }
  public String getReason() { return reason; } public String getPriority() { return priority; }
  public String getStatus() { return status; } public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setStatus(String s) { this.status = s; }
}
