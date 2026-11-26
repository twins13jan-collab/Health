package eg.gov.health.platform.emergencytriage;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TriageAssessmentRepository extends JpaRepository<TriageAssessment, UUID> {}
