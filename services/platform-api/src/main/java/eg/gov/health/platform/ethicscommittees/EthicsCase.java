package eg.gov.health.platform.ethicscommittees;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "ethics_cases")
public class EthicsCase {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="committee_id", nullable=false) private UUID committeeId;
  @Column(name="case_type") private String caseType;
  private String summary;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected EthicsCase() {}
  public EthicsCase(UUID facilityId, String governorate, UUID committeeId, String caseType, String summary) {
    this.facilityId=facilityId; this.governorate=governorate; this.committeeId=committeeId; this.caseType=caseType; this.summary=summary;
  }
  public UUID getId(){return id;} public UUID getCommitteeId(){return committeeId;} public String getStatus(){return status;}
  public void decide(){ this.status="decided"; this.updatedAt=OffsetDateTime.now(); }
}
