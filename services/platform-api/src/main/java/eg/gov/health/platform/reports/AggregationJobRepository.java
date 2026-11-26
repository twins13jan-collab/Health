package eg.gov.health.platform.reports;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AggregationJobRepository extends JpaRepository<AggregationJob, UUID> {
  List<AggregationJob> findBySnapshotIdOrderByCreatedAtDesc(UUID snapshotId);
}
