package eg.gov.health.platform.anomalyregistry;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AnomalyCaseRepository extends JpaRepository<AnomalyCase, UUID> {
  long countByFacilityIdAndClassification(UUID facilityId, String classification);
}
