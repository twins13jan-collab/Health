package eg.gov.health.platform.protocolversions;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ClinicalProtocolUsageRepository extends JpaRepository<ClinicalProtocolUsage, UUID> {
}
