package eg.gov.health.platform.vendormonitoring;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface VendorPerformanceRepository extends JpaRepository<VendorPerformance, UUID> {}
