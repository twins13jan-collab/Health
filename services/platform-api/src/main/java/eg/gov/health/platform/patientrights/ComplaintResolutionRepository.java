package eg.gov.health.platform.patientrights;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ComplaintResolutionRepository extends JpaRepository<ComplaintResolution, UUID> {
  List<ComplaintResolution> findByComplaintIdOrderByCreatedAtDesc(UUID complaintId);
}
