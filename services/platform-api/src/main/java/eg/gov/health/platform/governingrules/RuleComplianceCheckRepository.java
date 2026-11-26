package eg.gov.health.platform.governingrules;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface RuleComplianceCheckRepository extends JpaRepository<RuleComplianceCheck, UUID> {
  java.util.List<RuleComplianceCheck> findByScopeRef(String scopeRef);
}
