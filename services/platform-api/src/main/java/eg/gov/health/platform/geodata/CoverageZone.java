package eg.gov.health.platform.geodata;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "coverage_zones")
public class CoverageZone {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name;
  @Column(name="zone_type") private String zoneType;
  @Column(name="facility_ref") private String facilityRef;
  @Column(name="radius_km") private BigDecimal radiusKm;
  @Column(nullable=false) private boolean active = true;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CoverageZone() {}
  public CoverageZone(UUID facilityId, String governorate, String name, String zoneType, String facilityRef, BigDecimal radiusKm) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.zoneType=zoneType; this.facilityRef=facilityRef; this.radiusKm=radiusKm;
  }
  public UUID getId(){return id;}
}
