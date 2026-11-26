package eg.gov.health.platform.patienttransport;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PatientTransportRequestRepository extends JpaRepository<PatientTransportRequest, UUID> {}
