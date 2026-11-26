package eg.gov.health.platform.reports;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DataSnapshotRepository extends JpaRepository<DataSnapshot, UUID> {
  List<DataSnapshot> findByIndicatorIdOrderByCreatedAtDesc(UUID indicatorId);
}
