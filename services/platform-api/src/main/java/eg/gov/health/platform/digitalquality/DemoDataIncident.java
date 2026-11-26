package eg.gov.health.platform.digitalquality;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "demo_data_incidents")
public class DemoDataIncident {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String surface; private String description;
  @Column(nullable=false) private boolean resolved = false;
  @Column(name="detected_at") private OffsetDateTime detectedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DemoDataIncident() {}
  public DemoDataIncident(UUID facilityId, String governorate, String surface, String description) {
    this.facilityId=facilityId; this.governorate=governorate; this.surface=surface; this.description=description; this.detectedAt=OffsetDateTime.now();
  }
  public UUID getId(){return id;} public boolean isResolved(){return resolved;}
  public void resolve(){ this.resolved=true; this.updatedAt=OffsetDateTime.now(); }
}
