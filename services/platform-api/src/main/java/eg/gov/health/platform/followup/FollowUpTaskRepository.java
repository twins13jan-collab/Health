package eg.gov.health.platform.followup;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface FollowUpTaskRepository extends JpaRepository<FollowUpTask, UUID> {
  java.util.List<FollowUpTask> findByPlanId(UUID planId);
}
