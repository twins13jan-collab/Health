package eg.gov.health.platform.reports;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PublicationDatasetRepository extends JpaRepository<PublicationDataset, UUID> {
  List<PublicationDataset> findByJobIdOrderByCreatedAtDesc(UUID jobId);
}
