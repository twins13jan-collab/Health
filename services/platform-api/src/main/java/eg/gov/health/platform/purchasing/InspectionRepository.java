package eg.gov.health.platform.purchasing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface InspectionRepository extends JpaRepository<Inspection, UUID> {
  List<Inspection> findByReceiptIdOrderByCreatedAtDesc(UUID receiptId);
}
