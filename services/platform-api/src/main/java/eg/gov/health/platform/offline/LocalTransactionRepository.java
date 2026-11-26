package eg.gov.health.platform.offline;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface LocalTransactionRepository extends JpaRepository<LocalTransaction, UUID> {
  List<LocalTransaction> findByPackageIdOrderByCreatedAtAsc(UUID packageId);
  Optional<LocalTransaction> findByPackageIdAndLocalId(UUID packageId, String localId);
}
