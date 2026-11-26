package eg.gov.health.platform.dataquality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DqIndicatorDefinitionRepository extends JpaRepository<DqIndicatorDefinition, UUID> {}
