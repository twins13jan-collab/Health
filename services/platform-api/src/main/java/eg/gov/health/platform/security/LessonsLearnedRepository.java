package eg.gov.health.platform.security;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface LessonsLearnedRepository extends JpaRepository<LessonsLearned, UUID> {
  List<LessonsLearned> findByIncidentIdOrderByCreatedAtDesc(UUID incidentId);
}
