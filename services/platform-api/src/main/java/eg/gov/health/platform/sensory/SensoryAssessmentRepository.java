package eg.gov.health.platform.sensory;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SensoryAssessmentRepository extends JpaRepository<SensoryAssessment, UUID> {
  List<SensoryAssessment> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
