package eg.gov.health.platform.insurancedeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, UUID> {
  java.util.List<InsuranceClaim> findByFacilityId(UUID facilityId);
}
