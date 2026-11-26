package eg.gov.health.platform.nutrition;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface FeedOrderRepository extends JpaRepository<FeedOrder, UUID> {
  List<FeedOrder> findByPlanIdOrderByCreatedAtDesc(UUID planId);
}
