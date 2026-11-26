package eg.gov.health.platform.medicalassets;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, UUID> {
  java.util.List<MaintenancePlan> findByFacilityId(UUID facilityId);
}
