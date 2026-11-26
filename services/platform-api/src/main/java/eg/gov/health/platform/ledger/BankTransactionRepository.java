package eg.gov.health.platform.ledger;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID> {
  List<BankTransaction> findByStatementIdOrderByCreatedAtAsc(UUID statementId);
}
