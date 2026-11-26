package eg.gov.health.platform.cycles;
import jakarta.persistence.*; import java.time.OffsetDateTime; import java.util.UUID;
@Entity @Table(name = "cycle_events")
public class CycleEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name = "facility_id", nullable = false) private UUID facilityId;
  private String governorate;
  @Column(name = "instance_id", nullable = false) private UUID instanceId;
  @Column(name = "event_type", nullable = false) private String eventType;
  @Column(name = "from_status") private String fromStatus;
  @Column(name = "to_status") private String toStatus;
  @Column(name = "actor_id") private String actorId;
  @Column(name = "actor_role") private String actorRole;
  private String reason;
  @Column(nullable = false) private String status = "recorded";
  @Column(name = "created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name = "updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected CycleEvent() {}
  public CycleEvent(UUID facilityId, String governorate, UUID instanceId, String eventType, String fromStatus, String toStatus, String actorId, String actorRole, String reason) {
    this.facilityId = facilityId; this.governorate = governorate; this.instanceId = instanceId; this.eventType = eventType;
    this.fromStatus = fromStatus; this.toStatus = toStatus; this.actorId = actorId; this.actorRole = actorRole; this.reason = reason;
  }
  public UUID getId() { return id; } public UUID getInstanceId() { return instanceId; }
  public String getEventType() { return eventType; } public String getFromStatus() { return fromStatus; }
  public String getToStatus() { return toStatus; } public String getReason() { return reason; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
}
