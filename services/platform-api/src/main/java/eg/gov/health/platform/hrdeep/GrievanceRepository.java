package eg.gov.health.platform.hrdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface GrievanceRepository extends JpaRepository<Grievance, UUID> {
  java.util.List<Grievance> findByFacilityId(UUID facilityId);
}
