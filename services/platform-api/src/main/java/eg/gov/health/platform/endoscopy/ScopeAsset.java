package eg.gov.health.platform.endoscopy;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "scope_assets")
public class ScopeAsset {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="scope_code", nullable=false) private String scopeCode;
  @Column(name="scope_type") private String scopeType;
  @Column(nullable=false) private String status = "available";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ScopeAsset() {}
  public ScopeAsset(UUID facilityId, String governorate, String scopeCode, String scopeType) {
    this.facilityId=facilityId; this.governorate=governorate; this.scopeCode=scopeCode; this.scopeType=scopeType;
  }
  public UUID getId(){return id;} public String getStatus(){return status;}
  public UUID getFacilityId(){return facilityId;} public String getScopeCode(){return scopeCode;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
