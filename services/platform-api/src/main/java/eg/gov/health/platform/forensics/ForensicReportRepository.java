package eg.gov.health.platform.forensics;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ForensicReportRepository extends JpaRepository<ForensicReport, UUID> {}
