package eg.gov.health.platform.clinics;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface WaitlistRepository extends JpaRepository<ClinicWaitlistEntry, UUID> {
  List<ClinicWaitlistEntry> findBySessionIdAndStatusOrderByCreatedAtAsc(UUID sessionId, String status);
  long countBySessionIdAndStatus(UUID sessionId, String status);
}
