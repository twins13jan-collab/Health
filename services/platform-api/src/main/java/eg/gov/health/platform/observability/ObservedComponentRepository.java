package eg.gov.health.platform.observability;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ObservedComponentRepository extends JpaRepository<ObservedComponent, UUID> {}
