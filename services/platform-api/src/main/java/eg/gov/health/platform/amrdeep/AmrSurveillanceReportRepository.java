package eg.gov.health.platform.amrdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AmrSurveillanceReportRepository extends JpaRepository<AmrSurveillanceReport, UUID> {
}
