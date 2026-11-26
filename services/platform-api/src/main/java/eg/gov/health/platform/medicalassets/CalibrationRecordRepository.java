package eg.gov.health.platform.medicalassets;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface CalibrationRecordRepository extends JpaRepository<CalibrationRecord, UUID> {
  List<CalibrationRecord> findByAssetIdOrderByCreatedAtDesc(UUID assetId);
}
