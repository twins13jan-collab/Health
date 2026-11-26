package eg.gov.health.platform.orgchange;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface UserFeedbackItemRepository extends JpaRepository<UserFeedbackItem, UUID> {
}
