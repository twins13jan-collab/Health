package eg.gov.health.platform.clinicalai;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AiModelRegistryRepository extends JpaRepository<AiModelRegistry, UUID> {}
