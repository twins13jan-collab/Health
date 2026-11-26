package eg.gov.health.platform.patienttransport;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "transport_trips")
public class TransportTrip {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="request_id", nullable=false) private UUID requestId;
  @Column(name="departed_at") private OffsetDateTime departedAt;
  @Column(name="arrived_at") private OffsetDateTime arrivedAt;
  @Column(name="delay_minutes") private Integer delayMinutes;
  @Column(nullable=false) private boolean completed = false;
  private String complaint;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected TransportTrip() {}
  public TransportTrip(UUID facilityId, String governorate, UUID requestId, OffsetDateTime departedAt, OffsetDateTime arrivedAt,
                       Integer delayMinutes, boolean completed, String complaint) {
    this.facilityId=facilityId; this.governorate=governorate; this.requestId=requestId; this.departedAt=departedAt; this.arrivedAt=arrivedAt;
    this.delayMinutes=delayMinutes; this.completed=completed; this.complaint=complaint;
  }
  public UUID getId(){return id;}
}
