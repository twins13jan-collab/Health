package eg.gov.health.platform.uxgov;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface UsabilityTestSessionRepository extends JpaRepository<UsabilityTestSession, UUID> {
  boolean existsByScenarioIdAndTaskCompletedTrue(UUID scenarioId);
}
