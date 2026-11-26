package eg.gov.health.platform.mentalhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AssessmentInstrumentRepository extends JpaRepository<AssessmentInstrument, UUID> {
  List<AssessmentInstrument> findByEpisodeIdOrderByAssessedAtDesc(UUID episodeId);
}
