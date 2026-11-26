package eg.gov.health.platform.uxgov;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface UxFindingRepository extends JpaRepository<UxFinding, UUID> {
  long countByScenarioIdAndSeverityAndStatus(UUID scenarioId, String severity, String status);
}
