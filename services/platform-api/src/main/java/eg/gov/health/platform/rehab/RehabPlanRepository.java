package eg.gov.health.platform.rehab;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface RehabPlanRepository extends JpaRepository<RehabPlan, UUID> {
  List<RehabPlan> findByReferralIdOrderByCreatedAtDesc(UUID referralId);
}
