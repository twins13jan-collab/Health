package eg.gov.health.platform.criticalcare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface VentilationEpisodeRepository extends JpaRepository<VentilationEpisode, UUID> {
  List<VentilationEpisode> findByStayIdOrderByStartedAtDesc(UUID stayId);
}
