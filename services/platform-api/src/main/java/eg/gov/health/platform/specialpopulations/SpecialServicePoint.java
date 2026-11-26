package eg.gov.health.platform.specialpopulations;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "special_service_points")
public class SpecialServicePoint {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="site_type") private String siteType;
  private String name;
  @Column(name="safeguards_verified", nullable=false) private boolean safeguardsVerified = false;
  @Column(nullable=false) private String status = "planned";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SpecialServicePoint() {}
  public SpecialServicePoint(UUID facilityId, String governorate, String siteType, String name, boolean safeguardsVerified) {
    this.facilityId=facilityId; this.governorate=governorate; this.siteType=siteType; this.name=name; this.safeguardsVerified=safeguardsVerified;
  }
  public UUID getId(){return id;} public String getSiteType(){return siteType;} public boolean isSafeguardsVerified(){return safeguardsVerified;} public String getStatus(){return status;}
  public void activate(){ this.status="active"; this.updatedAt=OffsetDateTime.now(); }
}
