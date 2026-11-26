package eg.gov.health.platform.facilityenvironment;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SpillIncidentRepository extends JpaRepository<SpillIncident, UUID> {}
