package eg.gov.health.platform.nursing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface NursingAssessmentRepository extends JpaRepository<NursingAssessment, UUID> {
}
