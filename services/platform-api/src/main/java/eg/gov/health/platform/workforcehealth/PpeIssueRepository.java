package eg.gov.health.platform.workforcehealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PpeIssueRepository extends JpaRepository<PpeIssue, UUID> {}
