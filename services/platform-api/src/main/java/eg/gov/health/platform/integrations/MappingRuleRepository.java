package eg.gov.health.platform.integrations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface MappingRuleRepository extends JpaRepository<MappingRule, UUID> {
  List<MappingRule> findByEndpointIdOrderByCreatedAtAsc(UUID endpointId);
}
