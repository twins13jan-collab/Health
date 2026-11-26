package eg.gov.health.platform.traceability;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DataFlowRepository extends JpaRepository<DataFlow, UUID> {
  java.util.List<DataFlow> findByDataEntity(String dataEntity);
}
