package eg.gov.health.platform.journey;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "domain_events")
public class DomainEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="aggregate_type") private String aggregateType;
  @Column(name="aggregate_id") private UUID aggregateId;
  @Column(name="event_type") private String eventType;
  @Column(name="correlation_id") private String correlationId;
  private String payload;
  @Column(name="published_at") private OffsetDateTime publishedAt;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected DomainEvent() {}
  public DomainEvent(UUID facilityId, String governorate, String aggregateType, UUID aggregateId, String eventType, String correlationId, String payload) {
    this.facilityId=facilityId; this.governorate=governorate; this.aggregateType=aggregateType; this.aggregateId=aggregateId;
    this.eventType=eventType; this.correlationId=correlationId; this.payload=payload;
  }
  public UUID getId(){return id;} public String getEventType(){return eventType;} public String getCorrelationId(){return correlationId;}
  public OffsetDateTime getPublishedAt(){return publishedAt;}
  public void markPublished(){ this.publishedAt=OffsetDateTime.now(); }
}
