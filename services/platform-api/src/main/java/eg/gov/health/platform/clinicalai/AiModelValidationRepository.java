package eg.gov.health.platform.clinicalai;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AiModelValidationRepository extends JpaRepository<AiModelValidation, UUID> {
  List<AiModelValidation> findByModelIdOrderByCreatedAtDesc(UUID modelId);
}
