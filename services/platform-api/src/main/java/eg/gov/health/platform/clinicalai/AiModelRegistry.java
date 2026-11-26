package eg.gov.health.platform.clinicalai;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ai_model_registry")
public class AiModelRegistry {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(nullable=false) private String name;
  @Column(nullable=false) private String version;
  private String vendor; private String purpose;
  @Column(name="risk_class", nullable=false) private String riskClass = "moderate";
  @Column(name="training_data") private String trainingData;
  private String limitations; private String owner;
  @Column(name="committee_approved", nullable=false) private boolean committeeApproved = false;
  @Column(name="approved_scope") private String approvedScope;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected AiModelRegistry() {}
  public AiModelRegistry(UUID facilityId, String governorate, String name, String version, String vendor, String purpose, String riskClass, String trainingData, String limitations, String owner) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.version=version; this.vendor=vendor; this.purpose=purpose;
    this.riskClass=riskClass==null?"moderate":riskClass; this.trainingData=trainingData; this.limitations=limitations; this.owner=owner;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public boolean isCommitteeApproved(){return committeeApproved;} public String getVersion(){return version;} public String getRiskClass(){return riskClass;}
  public String getOwner(){return owner;}
  public void markValidated(){ this.status="validated"; this.updatedAt=OffsetDateTime.now(); }
  public void approve(String scope){ this.committeeApproved=true; this.approvedScope=scope; this.status="approved"; this.updatedAt=OffsetDateTime.now(); }
  public void suspend(){ this.status="suspended"; this.updatedAt=OffsetDateTime.now(); }
  public void reapprove(){ this.status="approved"; this.updatedAt=OffsetDateTime.now(); }
}
