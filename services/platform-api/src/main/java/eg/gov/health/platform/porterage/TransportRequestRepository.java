package eg.gov.health.platform.porterage;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TransportRequestRepository extends JpaRepository<TransportRequest, UUID> {  java.util.List<TransportRequest> findByFacilityIdOrderByRequestedAtAsc(java.util.UUID facilityId);
}
