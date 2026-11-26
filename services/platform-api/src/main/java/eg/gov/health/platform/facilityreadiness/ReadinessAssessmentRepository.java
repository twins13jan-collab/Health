package eg.gov.health.platform.facilityreadiness;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ReadinessAssessmentRepository extends JpaRepository<ReadinessAssessment, UUID> {}
