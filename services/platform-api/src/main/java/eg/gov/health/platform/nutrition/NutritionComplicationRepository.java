package eg.gov.health.platform.nutrition;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface NutritionComplicationRepository extends JpaRepository<NutritionComplication, UUID> {
  List<NutritionComplication> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
