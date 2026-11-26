package eg.gov.health.platform.dental;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DentalTreatmentPlanRepository extends JpaRepository<DentalTreatmentPlan, UUID> {
  List<DentalTreatmentPlan> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
