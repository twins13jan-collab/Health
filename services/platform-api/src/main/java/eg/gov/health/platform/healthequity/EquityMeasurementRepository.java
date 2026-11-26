package eg.gov.health.platform.healthequity;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface EquityMeasurementRepository extends JpaRepository<EquityMeasurement, UUID> {}
