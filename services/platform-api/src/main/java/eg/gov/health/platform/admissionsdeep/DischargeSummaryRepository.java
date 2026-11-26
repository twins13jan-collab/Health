package eg.gov.health.platform.admissionsdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DischargeSummaryRepository extends JpaRepository<DischargeSummary, UUID> {}
