package eg.gov.health.platform.mentalhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface MhRiskAssessmentRepository extends JpaRepository<MhRiskAssessment, UUID> {
  List<MhRiskAssessment> findByEpisodeIdOrderByAssessedAtDesc(UUID episodeId);
}
