package eg.gov.health.platform.emergency;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DispatchRequestRepository extends JpaRepository<DispatchRequest, UUID> {
  List<DispatchRequest> findByStatusOrderByCreatedAtAsc(String status);
}
