package eg.gov.health.platform.disasterops;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DisasterEventRepository extends JpaRepository<DisasterEvent, UUID> {
}
