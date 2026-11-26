package eg.gov.health.platform.softwarelifecycle;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "software_releases")
public class SoftwareRelease {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="component_id", nullable=false) private UUID componentId;
  private String version; private String environment;
  @Column(name="code_reviewed", nullable=false) private boolean codeReviewed = false;
  @Column(name="build_signed", nullable=false) private boolean buildSigned = false;
  @Column(name="config_drift", nullable=false) private boolean configDrift = false;
  @Column(name="rollback_reason") private String rollbackReason;
  @Column(nullable=false) private String status = "built";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SoftwareRelease() {}
  public SoftwareRelease(UUID facilityId, String governorate, UUID componentId, String version, String environment,
                         boolean codeReviewed, boolean buildSigned, boolean configDrift) {
    this.facilityId=facilityId; this.governorate=governorate; this.componentId=componentId; this.version=version; this.environment=environment;
    this.codeReviewed=codeReviewed; this.buildSigned=buildSigned; this.configDrift=configDrift;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getComponentId(){return componentId;}
  public boolean isCodeReviewed(){return codeReviewed;} public boolean isBuildSigned(){return buildSigned;} public boolean isConfigDrift(){return configDrift;}
  public void deploy(){ this.status="deployed"; this.updatedAt=OffsetDateTime.now(); }
  public void rollback(String reason){ this.status="rolled_back"; this.rollbackReason=reason; this.updatedAt=OffsetDateTime.now(); }
}
