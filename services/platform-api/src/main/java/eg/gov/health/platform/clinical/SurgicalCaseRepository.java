package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SurgicalCaseRepository extends JpaRepository<SurgicalCase, UUID> {
  List<SurgicalCase> findByFacilityIdOrderByScheduledAtAsc(UUID facilityId);
}
