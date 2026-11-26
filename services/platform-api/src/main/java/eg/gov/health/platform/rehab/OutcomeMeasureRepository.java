package eg.gov.health.platform.rehab;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface OutcomeMeasureRepository extends JpaRepository<OutcomeMeasure, UUID> {
  List<OutcomeMeasure> findByPlanIdOrderByMeasuredAtAsc(UUID planId);
}
