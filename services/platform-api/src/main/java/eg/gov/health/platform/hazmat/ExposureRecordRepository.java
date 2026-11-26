package eg.gov.health.platform.hazmat;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ExposureRecordRepository extends JpaRepository<ExposureRecord, UUID> {
  long countByIncidentIdAndFollowUpRequiredAndFollowUpDone(UUID incidentId, boolean required, boolean done);
}
