package eg.gov.health.platform.patientsafety;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SafetyIncidentRepository extends JpaRepository<SafetyIncident, UUID> {
}
