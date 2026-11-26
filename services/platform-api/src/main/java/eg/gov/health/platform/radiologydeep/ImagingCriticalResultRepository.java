package eg.gov.health.platform.radiologydeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ImagingCriticalResultRepository extends JpaRepository<ImagingCriticalResult, UUID> {
  java.util.List<ImagingCriticalResult> findByOrderId(UUID orderId);
  java.util.List<ImagingCriticalResult> findByFacilityIdAndAcknowledgedFalseOrderByCreatedAtAsc(UUID facilityId);
}
