package eg.gov.health.platform.institutionalrisk;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface RiskCommitteeDecisionRepository extends JpaRepository<RiskCommitteeDecision, UUID> {
}
