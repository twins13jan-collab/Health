package eg.gov.health.platform.patientrights;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ComplaintInvestigationRepository extends JpaRepository<ComplaintInvestigation, UUID> {
  List<ComplaintInvestigation> findByComplaintIdOrderByCreatedAtAsc(UUID complaintId);
}
