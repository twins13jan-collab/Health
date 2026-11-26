package eg.gov.health.platform.donationcampaigns;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "donation_campaigns")
public class DonationCampaign {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  private String name; private String site; private Integer capacity;
  @Column(name="campaign_date") private LocalDate campaignDate;
  @Column(name="units_collected", nullable=false) private int unitsCollected = 0;
  @Column(nullable=false) private String status = "planned";
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") private OffsetDateTime updatedAt = OffsetDateTime.now();
  protected DonationCampaign() {}
  public DonationCampaign(UUID facilityId, String governorate, String name, String site, Integer capacity, LocalDate campaignDate) {
    this.facilityId=facilityId; this.governorate=governorate; this.name=name; this.site=site; this.capacity=capacity; this.campaignDate=campaignDate;
  }
  public UUID getId(){return id;} public LocalDate getCampaignDate(){return campaignDate;} public int getUnitsCollected(){return unitsCollected;}
  public void addUnits(int n){ this.unitsCollected += n; this.updatedAt=OffsetDateTime.now(); }
}
