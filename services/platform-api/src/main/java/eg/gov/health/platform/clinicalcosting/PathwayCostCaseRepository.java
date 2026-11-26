package eg.gov.health.platform.clinicalcosting;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PathwayCostCaseRepository extends JpaRepository<PathwayCostCase, UUID> {
}
