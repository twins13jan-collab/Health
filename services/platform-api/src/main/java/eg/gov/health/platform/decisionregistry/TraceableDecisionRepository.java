package eg.gov.health.platform.decisionregistry;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TraceableDecisionRepository extends JpaRepository<TraceableDecision, UUID> {}
