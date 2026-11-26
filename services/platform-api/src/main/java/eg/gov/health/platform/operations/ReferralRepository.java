package eg.gov.health.platform.operations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ReferralRepository extends JpaRepository<Referral, UUID> {
  List<Referral> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  List<Referral> findByToFacilityIdAndStatus(UUID toFacilityId, String status);
}
