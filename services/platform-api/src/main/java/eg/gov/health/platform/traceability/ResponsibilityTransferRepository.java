package eg.gov.health.platform.traceability;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ResponsibilityTransferRepository extends JpaRepository<ResponsibilityTransfer, UUID> {
}
