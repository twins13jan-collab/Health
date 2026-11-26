package eg.gov.health.platform.integrations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface IntegrationEndpointRepository extends JpaRepository<IntegrationEndpoint, UUID> {}
