package eg.gov.health.platform.security;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, UUID> {
  List<NotificationRecord> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
