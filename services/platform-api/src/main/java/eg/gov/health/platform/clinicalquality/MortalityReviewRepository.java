package eg.gov.health.platform.clinicalquality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface MortalityReviewRepository extends JpaRepository<MortalityReview, UUID> {}
