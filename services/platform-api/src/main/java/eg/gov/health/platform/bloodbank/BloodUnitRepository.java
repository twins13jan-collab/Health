package eg.gov.health.platform.bloodbank;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface BloodUnitRepository extends JpaRepository<BloodUnit, UUID> {
  List<BloodUnit> findByFacilityIdAndStatus(UUID facilityId, String status);
  Optional<BloodUnit> findByUnitCode(String unitCode);
}
