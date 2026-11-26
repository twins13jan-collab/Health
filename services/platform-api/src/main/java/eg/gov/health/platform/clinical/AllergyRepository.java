package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AllergyRepository extends JpaRepository<Allergy, UUID> {
  List<Allergy> findByPatientIdAndStatus(UUID patientId, String status);
  List<Allergy> findByPatientId(UUID patientId);
}
