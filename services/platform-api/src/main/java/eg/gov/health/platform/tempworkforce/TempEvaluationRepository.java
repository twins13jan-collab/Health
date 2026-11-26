package eg.gov.health.platform.tempworkforce;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TempEvaluationRepository extends JpaRepository<TempEvaluation, UUID> {
}
