package eg.gov.health.platform.endoscopy;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ReprocessingCycleRepository extends JpaRepository<ReprocessingCycle, UUID> {
  List<ReprocessingCycle> findByScopeIdOrderByCompletedAtDesc(UUID scopeId);
}
