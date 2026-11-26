package eg.gov.health.platform.clinicalai;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AiModelIncidentRepository extends JpaRepository<AiModelIncident, UUID> {
  List<AiModelIncident> findByModelIdOrderByCreatedAtDesc(UUID modelId);
}
