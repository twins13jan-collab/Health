package eg.gov.health.platform.endoscopy;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ScopeAssetRepository extends JpaRepository<ScopeAsset, UUID> {
  Optional<ScopeAsset> findByScopeCode(String scopeCode);
}
