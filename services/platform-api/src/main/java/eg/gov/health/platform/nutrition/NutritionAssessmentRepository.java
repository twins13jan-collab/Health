package eg.gov.health.platform.nutrition;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface NutritionAssessmentRepository extends JpaRepository<NutritionAssessment, UUID> {
  List<NutritionAssessment> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
