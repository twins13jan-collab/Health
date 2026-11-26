package eg.gov.health.platform.ledger;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ChartOfAccountRepository extends JpaRepository<ChartOfAccount, UUID> {
  Optional<ChartOfAccount> findByAccountCode(String accountCode);
}
