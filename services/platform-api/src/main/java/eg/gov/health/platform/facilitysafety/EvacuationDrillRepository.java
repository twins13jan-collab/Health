package eg.gov.health.platform.facilitysafety;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface EvacuationDrillRepository extends JpaRepository<EvacuationDrill, UUID> {}
