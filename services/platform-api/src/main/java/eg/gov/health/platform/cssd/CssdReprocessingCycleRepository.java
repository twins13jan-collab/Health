package eg.gov.health.platform.cssd;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface CssdReprocessingCycleRepository extends JpaRepository<ReprocessingCycle, UUID> {
  java.util.List<ReprocessingCycle> findBySetIdOrderByCreatedAtDesc(UUID setId);
  java.util.List<ReprocessingCycle> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
}
