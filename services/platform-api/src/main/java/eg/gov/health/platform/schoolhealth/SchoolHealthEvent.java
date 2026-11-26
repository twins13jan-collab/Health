package eg.gov.health.platform.schoolhealth;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "school_health_events")
public class SchoolHealthEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="institution_ref") private String institutionRef;
  @Column(name="event_type") private String eventType;
  private String description;
  @Column(name="action_taken") private String actionTaken;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected SchoolHealthEvent() {}
  public SchoolHealthEvent(UUID facilityId, String governorate, String institutionRef, String eventType, String description, String actionTaken) {
    this.facilityId=facilityId; this.governorate=governorate; this.institutionRef=institutionRef; this.eventType=eventType;
    this.description=description; this.actionTaken=actionTaken;
  }
  public UUID getId(){return id;} public String getEventType(){return eventType;}
}
