package eg.gov.health.platform.eligibility;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ServiceAvailabilityCheckRepository extends JpaRepository<ServiceAvailabilityCheck, UUID> {}
