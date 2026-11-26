package eg.gov.health.platform.followup;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PendingResultReviewRepository extends JpaRepository<PendingResultReview, UUID> {
  java.util.List<PendingResultReview> findByPlanId(UUID planId);
}
