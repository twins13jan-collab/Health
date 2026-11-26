package eg.gov.health.platform.security;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface EvidenceItemRepository extends JpaRepository<EvidenceItem, UUID> {
  List<EvidenceItem> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
