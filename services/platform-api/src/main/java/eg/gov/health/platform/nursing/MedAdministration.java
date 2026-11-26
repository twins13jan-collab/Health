package eg.gov.health.platform.nursing;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/** One scheduled (then recorded) medication administration — the
 *  MAR row. Generated from an active prescription's frequency;
 *  recording is a nursing act with a mandatory reason whenever the
 *  dose is not given (refused / held / missed). */
@Entity @Table(name = "med_administrations")
public class MedAdministration {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "patient_id", nullable = false) private UUID patientId;
  @Column(name = "admission_id") private UUID admissionId;
  @Column(name = "prescription_id") private UUID prescriptionId;
  @Column(name = "drug_name", nullable = false) private String drugName;
  private String dose;
  @Column(name = "scheduled_at", nullable = false) private OffsetDateTime scheduledAt;
  @Column(nullable = false) private String status = "scheduled"; // scheduled, given, refused, held, missed
  @Column(name = "given_at") private OffsetDateTime givenAt;
  @Column(name = "recorded_by") private String recordedBy;
  private String reason;
  private String note;
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected MedAdministration() {}
  public MedAdministration(UUID facilityId, String governorate, UUID patientId, UUID admissionId,
                           UUID prescriptionId, String drugName, String dose, OffsetDateTime scheduledAt) {
    this.facilityId = facilityId; this.governorate = governorate; this.patientId = patientId;
    this.admissionId = admissionId; this.prescriptionId = prescriptionId; this.drugName = drugName;
    this.dose = dose; this.scheduledAt = scheduledAt;
  }
  public void record(String status, String reason, String note, String recordedBy) {
    this.status = status; this.reason = reason; this.note = note; this.recordedBy = recordedBy;
    if ("given".equals(status)) this.givenAt = OffsetDateTime.now();
  }
  public UUID getId() { return id; }
  public UUID getFacilityId() { return facilityId; }
  public UUID getPatientId() { return patientId; }
  public UUID getAdmissionId() { return admissionId; }
  public UUID getPrescriptionId() { return prescriptionId; }
  public String getDrugName() { return drugName; }
  public String getDose() { return dose; }
  public OffsetDateTime getScheduledAt() { return scheduledAt; }
  public String getStatus() { return status; }
  public OffsetDateTime getGivenAt() { return givenAt; }
  public String getRecordedBy() { return recordedBy; }
  public String getReason() { return reason; }
  public String getNote() { return note; }
}
