package eg.gov.health.platform.iamdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface EntitlementReviewRepository extends JpaRepository<EntitlementReview, UUID> {}
