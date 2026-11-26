package eg.gov.health.platform.radiologydeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ImagingReportRepository extends JpaRepository<ImagingReport, UUID> {
  java.util.List<ImagingReport> findByOrderId(UUID orderId);
}
