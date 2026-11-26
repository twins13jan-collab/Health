package eg.gov.health.platform.ledger;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {
  List<JournalLine> findByJournalIdOrderByCreatedAtAsc(UUID journalId);
}
