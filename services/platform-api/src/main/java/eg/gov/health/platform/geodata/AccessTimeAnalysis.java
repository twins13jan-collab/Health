package eg.gov.health.platform.geodata;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "access_time_analyses")
public class AccessTimeAnalysis {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="origin_ref") private String originRef;
  @Column(name="facility_ref") private String facilityRef;
  @Column(name="straight_line_km") private BigDecimal straightLineKm;
  @Column(name="road_distance_km") private BigDecimal roadDistanceKm;
  @Column(name="road_time_min") private Integer roadTimeMin;
  @Column(name="road_network_source") private String roadNetworkSource;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected AccessTimeAnalysis() {}
  public AccessTimeAnalysis(UUID facilityId, String governorate, String originRef, String facilityRef, BigDecimal straightLineKm,
                            BigDecimal roadDistanceKm, Integer roadTimeMin, String roadNetworkSource) {
    this.facilityId=facilityId; this.governorate=governorate; this.originRef=originRef; this.facilityRef=facilityRef;
    this.straightLineKm=straightLineKm; this.roadDistanceKm=roadDistanceKm; this.roadTimeMin=roadTimeMin; this.roadNetworkSource=roadNetworkSource;
  }
  public UUID getId(){return id;}
}
