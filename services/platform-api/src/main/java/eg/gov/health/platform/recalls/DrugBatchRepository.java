package eg.gov.health.platform.recalls;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DrugBatchRepository extends JpaRepository<DrugBatch, UUID> {
  List<DrugBatch> findByItemIdOrderByExpiryDateAsc(UUID itemId);
}
