package eg.gov.health.platform.purchasing;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "tenders")
public class Tender {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="request_id") private UUID requestId;
  @Column(nullable=false) private String title;
  @Column(name="committee_minutes") private String committeeMinutes;
  @Column(name="awarded_bid_id") private UUID awardedBidId;
  @Column(nullable=false) private String status = "open";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected Tender() {}
  public Tender(UUID facilityId, String governorate, UUID requestId, String title) {
    this.facilityId=facilityId; this.governorate=governorate; this.requestId=requestId; this.title=title;
  }
  public UUID getId(){return id;} public String getStatus(){return status;} public UUID getFacilityId(){return facilityId;}
  public void award(UUID bidId, String minutes){ this.awardedBidId=bidId; this.committeeMinutes=minutes; this.status="awarded"; this.updatedAt=OffsetDateTime.now(); }
}
