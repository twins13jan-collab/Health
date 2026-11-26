package eg.gov.health.platform.homecare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface HomeVisitRepository extends JpaRepository<HomeVisit, UUID> {
  List<HomeVisit> findByPlanIdOrderByScheduledAtAsc(UUID planId);
}
