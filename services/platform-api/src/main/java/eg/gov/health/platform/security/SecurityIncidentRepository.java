package eg.gov.health.platform.security;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SecurityIncidentRepository extends JpaRepository<SecurityIncident, UUID> {}
