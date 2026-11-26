package eg.gov.health.platform.medicalassets;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface MedicalAssetRepository extends JpaRepository<MedicalAsset, UUID> {
  java.util.List<MedicalAsset> findByFacilityId(UUID facilityId);
}
