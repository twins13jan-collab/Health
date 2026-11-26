package eg.gov.health.platform.organbank;
import jakarta.persistence.*; import java.time.*; import java.math.*; import java.util.UUID;
@Entity @Table(name = "custody_chain_events")
public class CustodyChainEvent {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="donation_id", nullable=false) private UUID donationId;
  private String stage; private String custodian;
  @Column(name="temperature_c") private BigDecimal temperatureC;
  @Column(name="event_at") private OffsetDateTime eventAt;
  @Column(name="ischemia_start", nullable=false) private boolean ischemiaStart = false;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected CustodyChainEvent() {}
  public CustodyChainEvent(UUID facilityId, String governorate, UUID donationId, String stage, String custodian, BigDecimal temperatureC, OffsetDateTime eventAt, boolean ischemiaStart) {
    this.facilityId=facilityId; this.governorate=governorate; this.donationId=donationId; this.stage=stage; this.custodian=custodian;
    this.temperatureC=temperatureC; this.eventAt=eventAt==null?OffsetDateTime.now():eventAt; this.ischemiaStart=ischemiaStart;
  }
  public UUID getId(){return id;} public boolean isIschemiaStart(){return ischemiaStart;} public OffsetDateTime getEventAt(){return eventAt;} public String getStage(){return stage;}
}
