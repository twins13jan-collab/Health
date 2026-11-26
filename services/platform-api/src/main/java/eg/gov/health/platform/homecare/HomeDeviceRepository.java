package eg.gov.health.platform.homecare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface HomeDeviceRepository extends JpaRepository<HomeDevice, UUID> {
  List<HomeDevice> findByFacilityIdAndSerialNumberAndStatusNot(UUID facilityId, String serialNumber, String status);
}
