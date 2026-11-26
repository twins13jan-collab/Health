package eg.gov.health.platform.nursing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface NursingCarePlanRepository extends JpaRepository<NursingCarePlan, UUID> {
}
