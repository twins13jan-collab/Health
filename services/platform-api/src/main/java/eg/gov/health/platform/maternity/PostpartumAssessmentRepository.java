package eg.gov.health.platform.maternity;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PostpartumAssessmentRepository extends JpaRepository<PostpartumAssessment, UUID> {
  List<PostpartumAssessment> findByPregnancyIdOrderByAssessedAtDesc(UUID pregnancyId);
}
