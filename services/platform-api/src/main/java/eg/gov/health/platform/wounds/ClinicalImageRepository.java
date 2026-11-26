package eg.gov.health.platform.wounds;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ClinicalImageRepository extends JpaRepository<ClinicalImage, UUID> {
  List<ClinicalImage> findByCaseIdOrderByCapturedAtDesc(UUID caseId);
}
