package eg.gov.health.platform.occhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ExposureIncidentRepository extends JpaRepository<ExposureIncident, UUID> {
  boolean existsByWorkerIdAndFollowUpExamDoneFalse(UUID workerId); java.util.List<ExposureIncident> findByWorkerId(UUID workerId);
}
