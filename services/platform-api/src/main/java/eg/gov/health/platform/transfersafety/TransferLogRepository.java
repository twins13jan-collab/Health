package eg.gov.health.platform.transfersafety;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TransferLogRepository extends JpaRepository<TransferLog, UUID> {}
