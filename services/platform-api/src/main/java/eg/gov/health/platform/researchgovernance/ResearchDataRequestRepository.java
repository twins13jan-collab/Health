package eg.gov.health.platform.researchgovernance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ResearchDataRequestRepository extends JpaRepository<ResearchDataRequest, UUID> {}
