package eg.gov.health.platform.mobiledevices;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DeviceSessionRepository extends JpaRepository<DeviceSession, UUID> {
  java.util.List<DeviceSession> findByDeviceId(UUID deviceId);
}
