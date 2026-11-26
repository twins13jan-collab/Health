package eg.gov.health.platform.quality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {
  List<Complaint> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
}
