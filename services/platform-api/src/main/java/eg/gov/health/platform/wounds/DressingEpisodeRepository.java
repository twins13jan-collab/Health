package eg.gov.health.platform.wounds;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DressingEpisodeRepository extends JpaRepository<DressingEpisode, UUID> {
  List<DressingEpisode> findByCaseIdOrderByCreatedAtDesc(UUID caseId);
}
