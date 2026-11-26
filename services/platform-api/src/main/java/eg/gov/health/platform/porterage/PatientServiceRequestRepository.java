package eg.gov.health.platform.porterage;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PatientServiceRequestRepository extends JpaRepository<PatientServiceRequest, UUID> {  java.util.List<PatientServiceRequest> findByFacilityIdOrderByCreatedAtAsc(java.util.UUID facilityId);
}
