package eg.gov.health.platform.wounds;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface WoundCaseRepository extends JpaRepository<WoundCase, UUID> {
  List<WoundCase> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
