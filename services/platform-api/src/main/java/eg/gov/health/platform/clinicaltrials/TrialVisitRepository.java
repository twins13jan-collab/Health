package eg.gov.health.platform.clinicaltrials;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TrialVisitRepository extends JpaRepository<TrialVisit, UUID> {}
