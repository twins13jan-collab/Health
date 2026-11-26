package eg.gov.health.platform.proms;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PromResponseRepository extends JpaRepository<PromResponse, UUID> {
  PromResponse findByScheduleId(UUID scheduleId);
}
