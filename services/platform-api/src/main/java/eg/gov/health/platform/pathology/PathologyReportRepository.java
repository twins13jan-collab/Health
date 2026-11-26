package eg.gov.health.platform.pathology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PathologyReportRepository extends JpaRepository<PathologyReport, UUID> {
  List<PathologyReport> findByAccessionIdOrderByCreatedAtDesc(UUID accessionId);
}
