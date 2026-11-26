package eg.gov.health.platform.nutrition;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface NutritionPlanRepository extends JpaRepository<NutritionPlan, UUID> {
  List<NutritionPlan> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
