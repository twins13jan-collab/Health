package eg.gov.health.platform.facility;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface FacilityRepository extends JpaRepository<Facility, UUID> {
  List<Facility> findByGovernorate(String governorate);
  List<Facility> findByOwnershipOrderByCreatedAtDesc(String ownership);
}
