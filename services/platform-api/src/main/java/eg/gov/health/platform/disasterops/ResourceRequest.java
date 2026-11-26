package eg.gov.health.platform.disasterops;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "resource_requests")
public class ResourceRequest {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="event_id", nullable=false) private UUID eventId;
  @Column(name="resource_type") private String resourceType;
  private Integer quantity;
  @Column(name="requesting_facility") private String requestingFacility;
  @Column(nullable=false) private String status = "requested";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected ResourceRequest() {}
  public ResourceRequest(UUID facilityId, String governorate, UUID eventId, String resourceType, Integer quantity, String requestingFacility) {
    this.facilityId=facilityId; this.governorate=governorate; this.eventId=eventId; this.resourceType=resourceType; this.quantity=quantity;
    this.requestingFacility=requestingFacility;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getEventId(){return eventId;}
  public void setStatus(String s){ this.status=s; this.updatedAt=OffsetDateTime.now(); }
}
