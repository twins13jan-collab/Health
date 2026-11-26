package eg.gov.health.platform.ledger;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface JournalRepository extends JpaRepository<Journal, UUID> {
  List<Journal> findByPeriodKeyAndStatus(String periodKey, String status);
  List<Journal> findByPeriodKeyAndStatusIn(String periodKey, java.util.Collection<String> statuses);
  List<Journal> findByStatus(String status);
  Optional<Journal> findBySourceRef(String sourceRef);
}
