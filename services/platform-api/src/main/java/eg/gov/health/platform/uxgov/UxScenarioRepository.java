package eg.gov.health.platform.uxgov;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface UxScenarioRepository extends JpaRepository<UxScenario, UUID> {
}
