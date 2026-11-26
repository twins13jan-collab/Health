package eg.gov.health.platform.facilitiesinfra;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "infra_systems")
public class InfraSystem {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="system_type") private String systemType;
  private String name;
  @Column(nullable=false) private boolean critical = false;
  @Column(nullable=false) private String status = "operational";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected InfraSystem() {}
  public InfraSystem(UUID facilityId, String governorate, String systemType, String name, boolean critical) {
    this.facilityId=facilityId; this.governorate=governorate; this.systemType=systemType; this.name=name; this.critical=critical;
  }
  public UUID getId(){return id;} public boolean isCritical(){return critical;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
