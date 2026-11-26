package eg.gov.health.platform.facilitysafety;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SafetyInspectionRepository extends JpaRepository<SafetyInspection, UUID> {}
