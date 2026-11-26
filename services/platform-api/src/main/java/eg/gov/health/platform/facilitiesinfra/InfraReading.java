package eg.gov.health.platform.facilitiesinfra;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "infra_readings")
public class InfraReading {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="system_id", nullable=false) private UUID systemId;
  private String metric; private Double value;
  @Column(name="within_limits", nullable=false) private boolean withinLimits = true;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected InfraReading() {}
  public InfraReading(UUID facilityId, String governorate, UUID systemId, String metric, Double value, boolean withinLimits) {
    this.facilityId=facilityId; this.governorate=governorate; this.systemId=systemId; this.metric=metric; this.value=value; this.withinLimits=withinLimits;
  }
  public UUID getId(){return id;} public boolean isWithinLimits(){return withinLimits;}
}
