package eg.gov.health.platform.wounds;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface WoundAssessmentRepository extends JpaRepository<WoundAssessment, UUID> {
  List<WoundAssessment> findByCaseIdOrderByAssessedAtAsc(UUID caseId);
}
