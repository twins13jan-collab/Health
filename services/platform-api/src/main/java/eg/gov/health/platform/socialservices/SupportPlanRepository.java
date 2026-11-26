package eg.gov.health.platform.socialservices;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SupportPlanRepository extends JpaRepository<SupportPlan, UUID> {
}
