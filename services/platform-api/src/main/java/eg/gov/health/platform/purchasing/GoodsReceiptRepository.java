package eg.gov.health.platform.purchasing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, UUID> {
  List<GoodsReceipt> findByPoIdOrderByCreatedAtAsc(UUID poId);
}
