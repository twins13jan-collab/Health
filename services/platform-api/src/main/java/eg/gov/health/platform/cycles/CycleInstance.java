package eg.gov.health.platform.cycles;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "cycle_instances")
public class CycleInstance {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "definition_code", nullable = false) private String definitionCode;
  @Column(name = "subject_type") private String subjectType;
  @Column(name = "subject_ref") private String subjectRef;
  @Column(name = "patient_id") private UUID patientId;
  @Column(nullable = false) private String title;
  @Column(name = "current_step") private String currentStep;
  @Column(name = "protocol_version") private String protocolVersion;
  @Column(name = "correlation_id", nullable = false) private String correlationId;
  @Column(name = "idempotency_key") private String idempotencyKey;
  @Column(name = "sla_due_at") private OffsetDateTime slaDueAt;
  private boolean escalated;
  @Column(nullable = false) private String status = "draft";
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CycleInstance() {}
  public CycleInstance(UUID facilityId, String governorate, String definitionCode, String subjectType, String subjectRef,
                       UUID patientId, String title, String protocolVersion, String correlationId, String idempotencyKey, OffsetDateTime slaDueAt) {
    this.facilityId = facilityId; this.governorate = governorate; this.definitionCode = definitionCode;
    this.subjectType = subjectType; this.subjectRef = subjectRef; this.patientId = patientId; this.title = title;
    this.protocolVersion = protocolVersion; this.correlationId = correlationId; this.idempotencyKey = idempotencyKey; this.slaDueAt = slaDueAt;
  }
  public UUID getId() { return id; } public UUID getFacilityId() { return facilityId; } public String getGovernorate() { return governorate; }
  public String getDefinitionCode() { return definitionCode; } public String getTitle() { return title; }
  public String getStatus() { return status; } public String getCorrelationId() { return correlationId; }
  public String getIdempotencyKey() { return idempotencyKey; } public OffsetDateTime getSlaDueAt() { return slaDueAt; }
  public boolean isEscalated() { return escalated; } public OffsetDateTime getCreatedAt() { return createdAt; }
  public String getSubjectType() { return subjectType; } public String getSubjectRef() { return subjectRef; }
  public UUID getPatientId() { return patientId; } public String getCurrentStep() { return currentStep; }
  public String getProtocolVersion() { return protocolVersion; }
  public void transitionTo(String to, String step) { this.status = to; if (step != null) this.currentStep = step; this.updatedAt = OffsetDateTime.now(); }
  public void escalate() { this.escalated = true; this.updatedAt = OffsetDateTime.now(); }
}
