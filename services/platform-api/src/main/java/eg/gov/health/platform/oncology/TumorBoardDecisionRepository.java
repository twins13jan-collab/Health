package eg.gov.health.platform.oncology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TumorBoardDecisionRepository extends JpaRepository<TumorBoardDecision, UUID> {}
