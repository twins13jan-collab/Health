package eg.gov.health.platform.portal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PortalServiceRequestRepository extends JpaRepository<PortalServiceRequest, UUID> {
  List<PortalServiceRequest> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
  List<PortalServiceRequest> findByFacilityIdAndStatusOrderByCreatedAtAsc(UUID facilityId, String status);
  List<PortalServiceRequest> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
}
