package eg.gov.health.platform.anomalyregistry;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TrendAnalysisRepository extends JpaRepository<TrendAnalysis, UUID> {
}
