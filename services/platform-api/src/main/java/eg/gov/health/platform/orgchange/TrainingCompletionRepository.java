package eg.gov.health.platform.orgchange;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TrainingCompletionRepository extends JpaRepository<TrainingCompletion, UUID> {
  long countByInitiativeIdAndCompletedTrue(UUID initiativeId);
}
