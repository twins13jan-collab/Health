package eg.gov.health.platform.pathology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PathologySecondOpinionRepository extends JpaRepository<PathologySecondOpinion, UUID> {
  List<PathologySecondOpinion> findByReportIdOrderByCreatedAtAsc(UUID reportId);
}
