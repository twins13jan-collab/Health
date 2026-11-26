package eg.gov.health.platform.finance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
  List<Invoice> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  List<Invoice> findByGovernorateOrderByCreatedAtDesc(String governorate);
  List<Invoice> findAllByOrderByCreatedAtDesc();
}
