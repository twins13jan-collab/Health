package eg.gov.health.platform.workforcehealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface WorkerExposureIncidentRepository extends JpaRepository<WorkerExposureIncident, UUID> {}
