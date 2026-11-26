package eg.gov.health.platform.medicalassets;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DeviceIncidentRepository extends JpaRepository<DeviceIncident, UUID> {
  List<DeviceIncident> findByAssetIdOrderByCreatedAtDesc(UUID assetId);
  List<DeviceIncident> findByFacilityId(UUID facilityId);
}
