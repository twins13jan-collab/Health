package eg.gov.health.platform.sensory;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface OphthalmicExamRepository extends JpaRepository<OphthalmicExam, UUID> {
  List<OphthalmicExam> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
