package eg.gov.health.platform.rehab;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface TherapySessionRepository extends JpaRepository<TherapySession, UUID> {
  List<TherapySession> findByPlanIdOrderBySessionAtAsc(UUID planId);
}
