package eg.gov.health.platform.clinicaltrials;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "trial_protocols")
public class TrialProtocol {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String title; private String phase; private String sponsor;
  @Column(name="protocol_version") private String protocolVersion;
  @Column(name="principal_investigator") private String principalInvestigator;
  @Column(name="ethics_approval_ref") private String ethicsApprovalRef;
  @Column(name="ethics_approved", nullable=false) private boolean ethicsApproved = false;
  @Column(name="regulatory_approval_ref") private String regulatoryApprovalRef;
  @Column(name="regulatory_approved", nullable=false) private boolean regulatoryApproved = false;
  @Column(name="coi_declared", nullable=false) private boolean coiDeclared = false;
  @Column(name="funding_ref") private String fundingRef;
  @Column(nullable=false) private String status = "draft";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected TrialProtocol() {}
  public TrialProtocol(UUID facilityId, String governorate, String title, String phase, String sponsor, String protocolVersion,
                       String principalInvestigator, String ethicsApprovalRef, boolean ethicsApproved, String regulatoryApprovalRef,
                       boolean regulatoryApproved, boolean coiDeclared, String fundingRef) {
    this.facilityId=facilityId; this.governorate=governorate; this.title=title; this.phase=phase; this.sponsor=sponsor;
    this.protocolVersion=protocolVersion; this.principalInvestigator=principalInvestigator; this.ethicsApprovalRef=ethicsApprovalRef;
    this.ethicsApproved=ethicsApproved; this.regulatoryApprovalRef=regulatoryApprovalRef; this.regulatoryApproved=regulatoryApproved;
    this.coiDeclared=coiDeclared; this.fundingRef=fundingRef;
  }
  public UUID getId(){return id;} public UUID getFacilityId(){return facilityId;} public String getStatus(){return status;}
  public boolean isEthicsApproved(){return ethicsApproved;} public boolean isRegulatoryApproved(){return regulatoryApproved;} public boolean isCoiDeclared(){return coiDeclared;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
