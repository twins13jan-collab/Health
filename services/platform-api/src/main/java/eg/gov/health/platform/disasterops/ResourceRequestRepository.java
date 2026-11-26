package eg.gov.health.platform.disasterops;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, UUID> {
  long countByEventIdAndStatusNot(UUID eventId, String status);
}
