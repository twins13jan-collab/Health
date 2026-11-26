package eg.gov.health.platform.integrations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "integration_endpoints")
public class IntegrationEndpoint {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String vendor; @Column(name="system_name", nullable=false) private String systemName;
  private String version; @Column(nullable=false) private String protocol;
  private String owner; @Column(nullable=false) private String environment = "test";
  @Column(name="compatibility_tested", nullable=false) private boolean compatibilityTested = false;
  @Column(nullable=false) private String status = "suspended";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected IntegrationEndpoint() {}
  public IntegrationEndpoint(UUID facilityId, String governorate, String vendor, String systemName, String version, String protocol, String owner, String environment, boolean compatibilityTested) {
    this.facilityId=facilityId; this.governorate=governorate; this.vendor=vendor; this.systemName=systemName; this.version=version;
    this.protocol=protocol; this.owner=owner; this.environment=environment==null?"test":environment; this.compatibilityTested=compatibilityTested;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public boolean isCompatibilityTested(){return compatibilityTested;}
  public UUID getFacilityId(){return facilityId;}
  public void markTested(){ this.compatibilityTested=true; this.updatedAt=OffsetDateTime.now(); }
  public void activate(){ this.status="active"; this.updatedAt=OffsetDateTime.now(); }
  public void setVersion(String v){ this.version=v; this.compatibilityTested=false; this.status="suspended"; this.updatedAt=OffsetDateTime.now(); }
}
