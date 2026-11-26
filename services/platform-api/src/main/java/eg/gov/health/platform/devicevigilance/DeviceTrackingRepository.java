package eg.gov.health.platform.devicevigilance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DeviceTrackingRepository extends JpaRepository<DeviceTracking, UUID> {
  java.util.Optional<DeviceTracking> findBySerialNo(String serialNo);
}
