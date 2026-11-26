package eg.gov.health.platform.connecteddevices;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ConnectedDeviceRepository extends JpaRepository<ConnectedDevice, UUID> {}
