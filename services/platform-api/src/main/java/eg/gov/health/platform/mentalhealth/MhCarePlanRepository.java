package eg.gov.health.platform.mentalhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface MhCarePlanRepository extends JpaRepository<MhCarePlan, UUID> {}
