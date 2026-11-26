package eg.gov.health.platform.continuity;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "failover_events")
public class FailoverEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="service_id", nullable=false) private UUID serviceId;
  private String mode; private String reason;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected FailoverEvent() {}
  public FailoverEvent(UUID facilityId, String governorate, UUID serviceId, String mode, String reason) {
    this.facilityId=facilityId; this.governorate=governorate; this.serviceId=serviceId; this.mode=mode; this.reason=reason;
  }
  public UUID getId(){return id;}
}
