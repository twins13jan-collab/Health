package eg.gov.health.platform.ledger;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriod, UUID> {
  Optional<AccountingPeriod> findByPeriodKey(String periodKey);
}
