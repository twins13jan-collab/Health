package eg.gov.health.platform.operations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AssetRepository extends JpaRepository<Asset, UUID> {
  List<Asset> findByFacilityIdOrderByNextMaintenanceAsc(UUID facilityId);
  List<Asset> findByFacilityIdAndNextMaintenanceLessThanEqual(UUID facilityId, java.time.LocalDate date);
}
