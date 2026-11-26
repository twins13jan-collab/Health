package eg.gov.health.platform.clinicalquality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ImprovementPlanRepository extends JpaRepository<ImprovementPlan, UUID> {}
