package eg.gov.health.platform.observability;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "component_metrics")
public class ComponentMetric {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="component_id", nullable=false) private UUID componentId;
  private String metric; private Double value; private Double threshold;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ComponentMetric() {}
  public ComponentMetric(UUID facilityId, String governorate, UUID componentId, String metric, Double value, Double threshold) {
    this.facilityId=facilityId; this.governorate=governorate; this.componentId=componentId; this.metric=metric; this.value=value; this.threshold=threshold;
  }
  public UUID getId(){return id;}
}
