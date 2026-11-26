package eg.gov.health.platform.protocolquality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ProtocolAdherenceReportRepository extends JpaRepository<ProtocolAdherenceReport, UUID> {
}
