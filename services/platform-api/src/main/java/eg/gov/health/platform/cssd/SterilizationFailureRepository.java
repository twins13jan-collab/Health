package eg.gov.health.platform.cssd;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SterilizationFailureRepository extends JpaRepository<SterilizationFailure, UUID> {
  java.util.List<SterilizationFailure> findByFacilityIdAndStatusOrderByCreatedAtDesc(UUID facilityId, String status);
}
