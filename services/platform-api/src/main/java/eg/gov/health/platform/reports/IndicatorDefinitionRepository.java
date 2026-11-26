package eg.gov.health.platform.reports;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface IndicatorDefinitionRepository extends JpaRepository<IndicatorDefinition, UUID> {
  List<IndicatorDefinition> findByCodeOrderByVersionDesc(String code);
}
