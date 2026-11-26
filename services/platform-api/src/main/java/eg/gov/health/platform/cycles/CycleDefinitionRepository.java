package eg.gov.health.platform.cycles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface CycleDefinitionRepository extends JpaRepository<CycleDefinition, UUID> {
  Optional<CycleDefinition> findByCode(String code);
  List<CycleDefinition> findByStatusOrderByCatalogNumber(String status);
}
