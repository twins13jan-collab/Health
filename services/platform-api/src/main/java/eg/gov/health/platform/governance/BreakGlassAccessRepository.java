package eg.gov.health.platform.governance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface BreakGlassAccessRepository extends JpaRepository<BreakGlassAccess, UUID> {
  List<BreakGlassAccess> findByStatusOrderByCreatedAtAsc(String status);
}
