package eg.gov.health.platform.forensics;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ForensicCaseRepository extends JpaRepository<ForensicCase, UUID> {}
