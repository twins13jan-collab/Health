package eg.gov.health.platform.ledger;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface CloseChecklistRepository extends JpaRepository<CloseChecklist, UUID> {
  List<CloseChecklist> findByPeriodKey(String periodKey);
}
