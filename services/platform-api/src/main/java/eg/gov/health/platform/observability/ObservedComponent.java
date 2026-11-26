package eg.gov.health.platform.observability;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "observed_components")
public class ObservedComponent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="component_ref") private String componentRef;
  @Column(name="component_type") private String componentType;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ObservedComponent() {}
  public ObservedComponent(UUID facilityId, String governorate, String componentRef, String componentType) {
    this.facilityId=facilityId; this.governorate=governorate; this.componentRef=componentRef; this.componentType=componentType;
  }
  public UUID getId(){return id;}
}
