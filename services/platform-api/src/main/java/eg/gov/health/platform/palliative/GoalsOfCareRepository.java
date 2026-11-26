package eg.gov.health.platform.palliative;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface GoalsOfCareRepository extends JpaRepository<GoalsOfCare, UUID> {
  List<GoalsOfCare> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
