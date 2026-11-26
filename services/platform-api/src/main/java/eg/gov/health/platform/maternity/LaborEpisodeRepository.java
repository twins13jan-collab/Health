package eg.gov.health.platform.maternity;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface LaborEpisodeRepository extends JpaRepository<LaborEpisode, UUID> {
  List<LaborEpisode> findByPregnancyIdOrderByStartedAtDesc(UUID pregnancyId);
}
