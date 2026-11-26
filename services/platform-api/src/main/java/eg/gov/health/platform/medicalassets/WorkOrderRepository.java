package eg.gov.health.platform.medicalassets;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {
  List<WorkOrder> findByAssetIdOrderByCreatedAtAsc(UUID assetId);
  List<WorkOrder> findByFacilityId(UUID facilityId);
}
