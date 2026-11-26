package eg.gov.health.platform.socialservices;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SocialAssessmentRepository extends JpaRepository<SocialAssessment, UUID> {
}
