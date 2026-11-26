package eg.gov.health.platform.devicevigilance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DeviceIncidentReportRepository extends JpaRepository<DeviceIncidentReport, UUID> {
}
