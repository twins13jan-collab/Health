package eg.gov.health.platform.governingrules;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface GoverningRuleRepository extends JpaRepository<GoverningRule, UUID> {
  java.util.List<GoverningRule> findByRuleCode(String ruleCode);
}
