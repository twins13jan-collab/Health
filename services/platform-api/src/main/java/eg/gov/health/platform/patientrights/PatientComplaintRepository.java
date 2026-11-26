package eg.gov.health.platform.patientrights;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PatientComplaintRepository extends JpaRepository<PatientComplaint, UUID> {}
