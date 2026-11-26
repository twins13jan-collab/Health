package eg.gov.health.platform.apimanagement;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "api_products")
public class ApiProduct {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="owner_ref") private String ownerRef;
  @Column(name="current_version") private String currentVersion;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ApiProduct() {}
  public ApiProduct(UUID facilityId, String governorate, String name, String ownerRef, String currentVersion) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.ownerRef=ownerRef; this.currentVersion=currentVersion;
  }
  public UUID getId(){return id;} public String getCurrentVersion(){return currentVersion;}
  public void setCurrentVersion(String v){ this.currentVersion=v; this.updatedAt=OffsetDateTime.now(); }
}
