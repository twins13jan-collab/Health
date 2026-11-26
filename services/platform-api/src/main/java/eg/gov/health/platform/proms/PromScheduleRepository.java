package eg.gov.health.platform.proms;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PromScheduleRepository extends JpaRepository<PromSchedule, UUID> {}
