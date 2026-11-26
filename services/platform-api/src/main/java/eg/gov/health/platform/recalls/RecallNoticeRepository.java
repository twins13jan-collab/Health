package eg.gov.health.platform.recalls;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface RecallNoticeRepository extends JpaRepository<RecallNotice, UUID> {}
