package eg.gov.health.platform.continuity;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface FailoverEventRepository extends JpaRepository<FailoverEvent, UUID> {
}
