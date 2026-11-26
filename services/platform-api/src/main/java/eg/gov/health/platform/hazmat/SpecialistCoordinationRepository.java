package eg.gov.health.platform.hazmat;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SpecialistCoordinationRepository extends JpaRepository<SpecialistCoordination, UUID> {
  boolean existsByIncidentId(UUID incidentId);
}
