package eg.gov.health.platform.donationcampaigns;
import jakarta.persistence.*; import java.time.*; import java.util.UUID;
@Entity @Table(name = "unit_allocations")
public class UnitAllocation {
  @Id @GeneratedValue private UUID id;
  @Column(name="facility_id", nullable=false) private UUID facilityId;
  private String governorate;
  @Column(name="campaign_id", nullable=false) private UUID campaignId;
  @Column(name="allocated_to") private String allocatedTo;
  @Column(name="units_allocated") private Integer unitsAllocated;
  @Column(name="created_at") private OffsetDateTime createdAt = OffsetDateTime.now();
  protected UnitAllocation() {}
  public UnitAllocation(UUID facilityId, String governorate, UUID campaignId, String allocatedTo, Integer unitsAllocated) {
    this.facilityId=facilityId; this.governorate=governorate; this.campaignId=campaignId; this.allocatedTo=allocatedTo; this.unitsAllocated=unitsAllocated;
  }
  public UUID getId(){return id;} public Integer getUnitsAllocated(){return unitsAllocated;}
}
