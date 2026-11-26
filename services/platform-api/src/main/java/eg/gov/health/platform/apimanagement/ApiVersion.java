package eg.gov.health.platform.apimanagement;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "api_versions")
public class ApiVersion {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="product_id", nullable=false) private UUID productId;
  private String version;
  @Column(name="backward_compatible", nullable=false) private boolean backwardCompatible = true;
  @Column(name="breaking_change_note") private String breakingChangeNote;
  @Column(nullable=false) private boolean deprecated = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ApiVersion() {}
  public ApiVersion(UUID facilityId, String governorate, UUID productId, String version, boolean backwardCompatible, String breakingChangeNote) {
    this.facilityId=facilityId; this.governorate=governorate; this.productId=productId; this.version=version;
    this.backwardCompatible=backwardCompatible; this.breakingChangeNote=breakingChangeNote;
  }
  public UUID getId(){return id;} public UUID getProductId(){return productId;} public String getVersion(){return version;} public boolean isBackwardCompatible(){return backwardCompatible;} public boolean isDeprecated(){return deprecated;}
  public void deprecate(){ this.deprecated=true; }
}
