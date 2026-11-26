package eg.gov.health.platform.vendormonitoring;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ExceptionalPurchaseRepository extends JpaRepository<ExceptionalPurchase, UUID> {}
