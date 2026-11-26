package eg.gov.health.platform.referralsdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ReferralRequestRepository extends JpaRepository<ReferralRequest, UUID> {
}
