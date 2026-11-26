package eg.gov.health.platform.researchgovernance;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "research_data_requests")
public class ResearchDataRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String purpose;
  @Column(name="necessity_justified", nullable=false) private boolean necessityJustified = false;
  @Column(name="minimal_dataset", nullable=false) private boolean minimalDataset = false;
  @Column(name="legal_basis") private String legalBasis;
  @Column(name="ethics_approved", nullable=false) private boolean ethicsApproved = false;
  @Column(name="data_form") private String dataForm;
  @Column(name="identified_justified", nullable=false) private boolean identifiedJustified = false;
  @Column(nullable=false) private String status = "pending";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ResearchDataRequest() {}
  public ResearchDataRequest(UUID facilityId, String governorate, String purpose, boolean necessityJustified, boolean minimalDataset,
                                 String legalBasis, boolean ethicsApproved, String dataForm, boolean identifiedJustified) {
    this.facilityId=facilityId; this.governorate=governorate; this.purpose=purpose; this.necessityJustified=necessityJustified;
    this.minimalDataset=minimalDataset; this.legalBasis=legalBasis; this.ethicsApproved=ethicsApproved; this.dataForm=dataForm;
    this.identifiedJustified=identifiedJustified;
  }
  public UUID getId(){return id;} public String getPurpose(){return purpose;} public boolean isNecessityJustified(){return necessityJustified;}
  public boolean isMinimalDataset(){return minimalDataset;} public String getLegalBasis(){return legalBasis;}
  public boolean isEthicsApproved(){return ethicsApproved;} public String getDataForm(){return dataForm;}
  public boolean isIdentifiedJustified(){return identifiedJustified;} public String getStatus(){return status;}
  public void approve(){ this.status="approved"; this.updatedAt=OffsetDateTime.now(); }
}
