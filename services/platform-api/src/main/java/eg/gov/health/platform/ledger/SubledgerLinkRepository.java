package eg.gov.health.platform.ledger;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SubledgerLinkRepository extends JpaRepository<SubledgerLink, UUID> {
  List<SubledgerLink> findByJournalId(UUID journalId);
}
