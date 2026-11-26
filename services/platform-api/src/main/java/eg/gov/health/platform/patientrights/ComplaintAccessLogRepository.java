package eg.gov.health.platform.patientrights;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ComplaintAccessLogRepository extends JpaRepository<ComplaintAccessLog, UUID> {
  List<ComplaintAccessLog> findByComplaintIdOrderByCreatedAtAsc(UUID complaintId);
}
