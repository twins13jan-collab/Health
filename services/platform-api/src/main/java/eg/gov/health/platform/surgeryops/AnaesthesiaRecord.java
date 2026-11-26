package eg.gov.health.platform.surgeryops;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/** The anaesthesia record of a surgical case: ASA class, technique,
 *  anaesthetist, and the pre-operative assessment flag that gates
 *  starting an elective case. One record per case. */
@Entity @Table(name = "anaesthesia_records")
public class AnaesthesiaRecord {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  @Column(name = "case_id", nullable = false, unique = true) private UUID caseId;
  @Column(name = "patient_id", nullable = false) private UUID patientId;
  @Column(name = "asa_class") private Integer asaClass;
  @Column(name = "anaesthesia_type") private String anaesthesiaType; // general, spinal, epidural, local, sedation
  private String anaesthetist;
  @Column(name = "preop_assessment", nullable = false) private boolean preopAssessment = false;
  @Column(name = "started_at") private OffsetDateTime startedAt;
  @Column(name = "ended_at") private OffsetDateTime endedAt;
  private String complications;
  private String notes;
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AnaesthesiaRecord() {}
  public AnaesthesiaRecord(UUID facilityId, UUID caseId, UUID patientId) {
    this.facilityId = facilityId; this.caseId = caseId; this.patientId = patientId;
  }
  public void update(Integer asa, String type, String anaesthetist, Boolean preop, String complications, String notes) {
    if (asa != null) this.asaClass = asa;
    if (type != null) this.anaesthesiaType = type;
    if (anaesthetist != null) this.anaesthetist = anaesthetist;
    if (preop != null) this.preopAssessment = preop;
    if (complications != null) this.complications = complications;
    if (notes != null) this.notes = notes;
    this.updatedAt = OffsetDateTime.now();
  }
  public void start() { if (this.startedAt == null) this.startedAt = OffsetDateTime.now(); this.updatedAt = OffsetDateTime.now(); }
  public void end() { this.endedAt = OffsetDateTime.now(); this.updatedAt = OffsetDateTime.now(); }
  public UUID getId() { return id; }
  public UUID getCaseId() { return caseId; }
  public UUID getPatientId() { return patientId; }
  public Integer getAsaClass() { return asaClass; }
  public String getAnaesthesiaType() { return anaesthesiaType; }
  public String getAnaesthetist() { return anaesthetist; }
  public boolean isPreopAssessment() { return preopAssessment; }
  public OffsetDateTime getStartedAt() { return startedAt; }
  public OffsetDateTime getEndedAt() { return endedAt; }
  public String getComplications() { return complications; }
  public String getNotes() { return notes; }
}
