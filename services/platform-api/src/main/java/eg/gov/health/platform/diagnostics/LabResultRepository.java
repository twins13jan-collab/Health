package eg.gov.health.platform.diagnostics;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface LabResultRepository extends JpaRepository<LabResult, UUID> {
  List<LabResult> findByOrderIdOrderByVersionDesc(UUID orderId);
  List<LabResult> findByFacilityIdAndCriticalTrueAndStatusNot(UUID facilityId, String status);
}
