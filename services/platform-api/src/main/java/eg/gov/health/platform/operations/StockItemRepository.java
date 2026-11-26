package eg.gov.health.platform.operations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface StockItemRepository extends JpaRepository<StockItem, UUID> {
  Optional<StockItem> findByFacilityIdAndItemCode(UUID facilityId, String itemCode);
  List<StockItem> findByFacilityIdOrderByItemCode(UUID facilityId);
}
