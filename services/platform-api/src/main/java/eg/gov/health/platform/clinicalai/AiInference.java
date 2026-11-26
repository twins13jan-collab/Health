package eg.gov.health.platform.clinicalai;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ai_inferences")
public class AiInference {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="model_id", nullable=false) private UUID modelId;
  @Column(name="model_version") private String modelVersion;
  @Column(name="patient_ref") private String patientRef;
  @Column(name="inputs_summary") private String inputsSummary;
  @Column(name="inputs_complete", nullable=false) private boolean inputsComplete = false;
  @Column(name="in_scope", nullable=false) private boolean inScope = false;
  private String result; private java.math.BigDecimal confidence;
  @Column(nullable=false) private boolean abstained = false;
  @Column(name="abstain_reason") private String abstainReason;
  @Column(name="requested_by") private String requestedBy;
  @Column(nullable=false) private boolean applied = false;
  @Column(nullable=false) private String status = "pending_review";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AiInference() {}
  public AiInference(UUID facilityId, String governorate, UUID modelId, String modelVersion, String patientRef, String inputsSummary,
                     boolean inputsComplete, boolean inScope, String result, java.math.BigDecimal confidence, String requestedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.modelId=modelId; this.modelVersion=modelVersion; this.patientRef=patientRef;
    this.inputsSummary=inputsSummary; this.inputsComplete=inputsComplete; this.inScope=inScope; this.result=result;
    this.confidence=confidence; this.requestedBy=requestedBy;
    if (AiRules.mustAbstain(inputsComplete, inScope)) {
      this.abstained=true; this.status="abstained"; this.result=null;
      this.abstainReason = !inputsComplete ? "incomplete inputs — the model abstains rather than guess"
                                            : "outside the validated scope — the model abstains";
    }
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public boolean isAbstained(){return abstained;}
  public UUID getFacilityId(){return facilityId;}
  public UUID getModelId(){return modelId;} public boolean isApplied(){return applied;}
  public void markReviewed(){ this.status="reviewed"; this.updatedAt=OffsetDateTime.now(); }
  public void apply(){ this.applied=true; this.updatedAt=OffsetDateTime.now(); }
}
