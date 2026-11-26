package eg.gov.health.platform.hazmat;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "specialist_coordination")
public class SpecialistCoordination {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="incident_id", nullable=false) private UUID incidentId;
  @Column(name="coordinated_with") private String coordinatedWith;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected SpecialistCoordination() {}
  public SpecialistCoordination(UUID facilityId, String governorate, UUID incidentId, String coordinatedWith) {
    this.facilityId=facilityId; this.governorate=governorate; this.incidentId=incidentId; this.coordinatedWith=coordinatedWith;
  }
  public UUID getId(){return id;}
}
