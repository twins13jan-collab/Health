package eg.gov.health.platform.complexcare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CareTransitionRepository extends JpaRepository<CareTransition, UUID> {
}
