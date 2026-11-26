package eg.gov.health.platform.endoscopy;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface EndoscopyRequestRepository extends JpaRepository<EndoscopyRequest, UUID> {
  List<EndoscopyRequest> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
