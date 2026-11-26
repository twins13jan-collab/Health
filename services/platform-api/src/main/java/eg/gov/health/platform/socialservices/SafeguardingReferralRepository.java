package eg.gov.health.platform.socialservices;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SafeguardingReferralRepository extends JpaRepository<SafeguardingReferral, UUID> {
  boolean existsByAssessmentId(UUID assessmentId);
}
