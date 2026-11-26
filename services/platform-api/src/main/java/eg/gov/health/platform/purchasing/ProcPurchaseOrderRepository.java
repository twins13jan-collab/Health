package eg.gov.health.platform.purchasing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ProcPurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {}
