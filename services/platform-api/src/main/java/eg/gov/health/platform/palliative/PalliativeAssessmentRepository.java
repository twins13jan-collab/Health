package eg.gov.health.platform.palliative;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PalliativeAssessmentRepository extends JpaRepository<PalliativeAssessment, UUID> {
  List<PalliativeAssessment> findByPatientIdOrderByAssessedAtDesc(UUID patientId);
}
