package eg.gov.health.platform.toxicology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PoisoningCaseRepository extends JpaRepository<PoisoningCase, UUID> {
  List<PoisoningCase> findAllByOrderByCreatedAtDesc();
  List<PoisoningCase> findByExposureEventId(UUID exposureEventId);
}
