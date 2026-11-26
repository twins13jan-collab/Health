package eg.gov.health.platform.offline;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ConflictCaseRepository extends JpaRepository<ConflictCase, UUID> {
  List<ConflictCase> findByTransactionId(UUID transactionId);
  List<ConflictCase> findByStatusOrderByCreatedAtAsc(String status);
}
