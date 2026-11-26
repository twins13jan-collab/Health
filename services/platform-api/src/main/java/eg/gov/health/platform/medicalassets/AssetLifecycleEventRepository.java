package eg.gov.health.platform.medicalassets;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AssetLifecycleEventRepository extends JpaRepository<AssetLifecycleEvent, UUID> {
  List<AssetLifecycleEvent> findByAssetIdOrderByCreatedAtAsc(UUID assetId);
}
