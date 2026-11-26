package eg.gov.health.platform.insurancedeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ClaimAppealRepository extends JpaRepository<ClaimAppeal, UUID> {
  java.util.List<ClaimAppeal> findByFacilityId(UUID facilityId);
}
