package eg.gov.health.platform.security;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface IncidentActionRepository extends JpaRepository<IncidentAction, UUID> {
  List<IncidentAction> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
