package eg.gov.health.platform.security;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PrivacyImpactRepository extends JpaRepository<PrivacyImpact, UUID> {
  List<PrivacyImpact> findByIncidentIdOrderByCreatedAtDesc(UUID incidentId);
}
