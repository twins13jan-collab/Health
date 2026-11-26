package eg.gov.health.platform.donationcampaigns;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DonationCampaignRepository extends JpaRepository<DonationCampaign, UUID> {
}
