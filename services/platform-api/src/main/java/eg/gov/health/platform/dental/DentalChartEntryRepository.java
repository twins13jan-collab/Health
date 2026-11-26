package eg.gov.health.platform.dental;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DentalChartEntryRepository extends JpaRepository<DentalChartEntry, UUID> {
  List<DentalChartEntry> findByChartIdOrderByToothNumberAsc(UUID chartId);
}
