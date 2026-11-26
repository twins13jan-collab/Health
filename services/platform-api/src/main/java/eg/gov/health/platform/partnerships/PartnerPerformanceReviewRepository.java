package eg.gov.health.platform.partnerships;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PartnerPerformanceReviewRepository extends JpaRepository<PartnerPerformanceReview, UUID> {
}
