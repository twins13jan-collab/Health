package eg.gov.health.platform.clinicalai;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AiHumanReviewRepository extends JpaRepository<AiHumanReview, UUID> {
  List<AiHumanReview> findByInferenceIdOrderByCreatedAtDesc(UUID inferenceId);
}
