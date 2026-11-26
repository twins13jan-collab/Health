package eg.gov.health.platform.sensory;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AudiologyTestRepository extends JpaRepository<AudiologyTest, UUID> {
  List<AudiologyTest> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
