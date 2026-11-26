package eg.gov.health.platform.offline;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DeviceTrustRepository extends JpaRepository<DeviceTrust, UUID> {
  Optional<DeviceTrust> findByDeviceCode(String deviceCode);
}
