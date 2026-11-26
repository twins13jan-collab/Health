package eg.gov.health.platform.finance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, UUID> {
  List<InvoiceLine> findByInvoiceId(UUID invoiceId);
}
