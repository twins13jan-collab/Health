package eg.gov.health.platform.recalls;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
  List<StockMovement> findByBatchIdOrderByCreatedAtAsc(UUID batchId);
}
