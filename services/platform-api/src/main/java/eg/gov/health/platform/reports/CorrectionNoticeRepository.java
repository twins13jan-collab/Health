package eg.gov.health.platform.reports;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CorrectionNoticeRepository extends JpaRepository<CorrectionNotice, UUID> {}
