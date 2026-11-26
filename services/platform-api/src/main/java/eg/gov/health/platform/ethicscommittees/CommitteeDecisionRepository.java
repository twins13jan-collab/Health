package eg.gov.health.platform.ethicscommittees;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CommitteeDecisionRepository extends JpaRepository<CommitteeDecision, UUID> {
}
