package eg.gov.health.platform.purchasing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, UUID> {}
