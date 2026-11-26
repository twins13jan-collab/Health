package eg.gov.health.platform.homecare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface HomeCarePlanRepository extends JpaRepository<HomeCarePlan, UUID> {
  List<HomeCarePlan> findByReferralIdOrderByCreatedAtDesc(UUID referralId);
}
