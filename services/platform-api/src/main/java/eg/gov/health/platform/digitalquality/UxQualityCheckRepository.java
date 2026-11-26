package eg.gov.health.platform.digitalquality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface UxQualityCheckRepository extends JpaRepository<UxQualityCheck, UUID> {
  java.util.List<UxQualityCheck> findBySurfaceOrderByCreatedAtDesc(String surface);
}
