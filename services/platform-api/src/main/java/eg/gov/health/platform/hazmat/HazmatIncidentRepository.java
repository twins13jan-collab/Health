package eg.gov.health.platform.hazmat;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface HazmatIncidentRepository extends JpaRepository<HazmatIncident, UUID> {
}
