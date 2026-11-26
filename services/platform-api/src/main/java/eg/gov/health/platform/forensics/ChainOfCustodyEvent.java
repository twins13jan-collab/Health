package eg.gov.health.platform.forensics;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "chain_of_custody_events")
public class ChainOfCustodyEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="evidence_id", nullable=false) private UUID evidenceId;
  @Column(name="event_type") private String eventType;
  @Column(name="from_party") private String fromParty;
  @Column(name="to_party") private String toParty;
  @Column(name="recorded_by") private String recordedBy;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected ChainOfCustodyEvent() {}
  public ChainOfCustodyEvent(UUID facilityId, String governorate, UUID evidenceId, String eventType, String fromParty, String toParty, String recordedBy) {
    this.facilityId=facilityId; this.governorate=governorate; this.evidenceId=evidenceId; this.eventType=eventType;
    this.fromParty=fromParty; this.toParty=toParty; this.recordedBy=recordedBy;
  }
  public UUID getId(){return id;}
}
