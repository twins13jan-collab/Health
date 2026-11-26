package eg.gov.health.platform.surgeryops;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/** A patient waiting for a surgical procedure. Priority is
 *  clinical (emergency, urgent, elective); the list order is
 *  priority first, then longest waiting — never arrival of the
 *  loudest voice. Scheduling creates the surgical case and links
 *  it here; cancellation requires a documented reason. */
@Entity @Table(name = "surgical_waitlist")
public class SurgicalWaitlistEntry {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "patient_id", nullable = false) private UUID patientId;
  @Column(name = "procedure_name", nullable = false) private String procedureName;
  private String specialty;
  @Column(nullable = false) private String priority = "elective"; // emergency, urgent, elective
  @Column(name = "requested_by") private String requestedBy;
  private String notes;
  @Column(nullable = false) private String status = "waiting"; // waiting, scheduled, done, cancelled
  @Column(name = "scheduled_case_id") private UUID scheduledCaseId;
  @Column(name = "cancel_reason") private String cancelReason;
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SurgicalWaitlistEntry() {}
  public SurgicalWaitlistEntry(UUID facilityId, String governorate, UUID patientId, String procedureName,
                               String specialty, String priority, String requestedBy, String notes) {
    this.facilityId = facilityId; this.governorate = governorate; this.patientId = patientId;
    this.procedureName = procedureName; this.specialty = specialty;
    this.priority = priority == null ? "elective" : priority;
    this.requestedBy = requestedBy; this.notes = notes;
  }
  public void markScheduled(UUID caseId) { this.status = "scheduled"; this.scheduledCaseId = caseId; this.updatedAt = OffsetDateTime.now(); }
  public void markDone() { this.status = "done"; this.updatedAt = OffsetDateTime.now(); }
  public void cancel(String reason) { this.status = "cancelled"; this.cancelReason = reason; this.updatedAt = OffsetDateTime.now(); }
  public UUID getId() { return id; }
  public UUID getFacilityId() { return facilityId; }
  public UUID getPatientId() { return patientId; }
  public String getProcedureName() { return procedureName; }
  public String getSpecialty() { return specialty; }
  public String getPriority() { return priority; }
  public String getStatus() { return status; }
  public UUID getScheduledCaseId() { return scheduledCaseId; }
  public String getCancelReason() { return cancelReason; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
