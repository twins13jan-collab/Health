package eg.gov.health.platform.operations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
  List<PurchaseOrder> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
}
