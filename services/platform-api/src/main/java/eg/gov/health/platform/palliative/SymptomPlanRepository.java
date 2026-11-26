package eg.gov.health.platform.palliative;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SymptomPlanRepository extends JpaRepository<SymptomPlan, UUID> {
  List<SymptomPlan> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
