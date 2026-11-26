package eg.gov.health.platform.cycles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface CycleEventRepository extends JpaRepository<CycleEvent, UUID> {
  List<CycleEvent> findByInstanceIdOrderByCreatedAtAsc(UUID instanceId);
}
