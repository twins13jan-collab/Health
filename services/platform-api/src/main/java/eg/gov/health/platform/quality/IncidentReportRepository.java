package eg.gov.health.platform.quality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface IncidentReportRepository extends JpaRepository<IncidentReport, UUID> {
  List<IncidentReport> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  List<IncidentReport> findByFacilityIdAndStatus(UUID facilityId, String status);
}
