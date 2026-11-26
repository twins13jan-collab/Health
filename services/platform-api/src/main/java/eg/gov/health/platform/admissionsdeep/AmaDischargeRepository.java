package eg.gov.health.platform.admissionsdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AmaDischargeRepository extends JpaRepository<AmaDischarge, UUID> {}
