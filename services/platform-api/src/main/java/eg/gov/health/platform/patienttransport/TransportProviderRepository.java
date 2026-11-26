package eg.gov.health.platform.patienttransport;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TransportProviderRepository extends JpaRepository<TransportProvider, UUID> {}
