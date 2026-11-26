package eg.gov.health.platform.purchasing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, UUID> {}
