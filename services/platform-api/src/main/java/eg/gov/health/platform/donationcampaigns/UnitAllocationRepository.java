package eg.gov.health.platform.donationcampaigns;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface UnitAllocationRepository extends JpaRepository<UnitAllocation, UUID> {
  java.util.List<UnitAllocation> findByCampaignId(UUID campaignId);
}
