package eg.gov.health.platform.toxicology;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "exposure_events")
public class ExposureEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="location_zone") private String locationZone;
  @Column(name="affected_count", nullable=false) private int affectedCount = 0;
  @Column(name="suspected_source") private String suspectedSource;
  @Column(nullable=false) private String status = "reported";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ExposureEvent() {}
  public ExposureEvent(UUID facilityId, String governorate, String locationZone, int affectedCount, String suspectedSource) {
    this.facilityId=facilityId; this.governorate=governorate; this.locationZone=locationZone;
    this.affectedCount=affectedCount; this.suspectedSource=suspectedSource;
  }
  public UUID getId(){return id;} public int getAffectedCount(){return affectedCount;}
}
