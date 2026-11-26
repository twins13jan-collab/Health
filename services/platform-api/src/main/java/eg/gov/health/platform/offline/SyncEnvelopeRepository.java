package eg.gov.health.platform.offline;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SyncEnvelopeRepository extends JpaRepository<SyncEnvelope, UUID> {
  Optional<SyncEnvelope> findByPackageIdAndSyncId(UUID packageId, String syncId);
}
