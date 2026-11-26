package eg.gov.health.platform.palliative;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface BereavementSupportRepository extends JpaRepository<BereavementSupport, UUID> {
  List<BereavementSupport> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
