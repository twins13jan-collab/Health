package eg.gov.health.platform.rehab;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DischargeRecommendationRepository extends JpaRepository<DischargeRecommendation, UUID> {}
