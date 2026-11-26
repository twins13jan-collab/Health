package eg.gov.health.platform.clinicalai;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ai_model_validations")
public class AiModelValidation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="model_id", nullable=false) private UUID modelId;
  @Column(name="dataset_description") private String datasetDescription;
  private String population; private String performance;
  @Column(name="bias_assessment") private String biasAssessment;
  @Column(nullable=false) private boolean approved = false;
  private String committee;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AiModelValidation() {}
  public AiModelValidation(UUID facilityId, String governorate, UUID modelId, String datasetDescription, String population, String performance, String biasAssessment, boolean approved, String committee) {
    this.facilityId=facilityId; this.governorate=governorate; this.modelId=modelId; this.datasetDescription=datasetDescription;
    this.population=population; this.performance=performance; this.biasAssessment=biasAssessment; this.approved=approved; this.committee=committee;
  }
  public UUID getId(){return id;} public boolean isApproved(){return approved;}
}
