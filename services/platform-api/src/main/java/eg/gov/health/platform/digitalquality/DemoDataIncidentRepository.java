package eg.gov.health.platform.digitalquality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DemoDataIncidentRepository extends JpaRepository<DemoDataIncident, UUID> {
  long countBySurfaceAndResolvedFalse(String surface);
}
