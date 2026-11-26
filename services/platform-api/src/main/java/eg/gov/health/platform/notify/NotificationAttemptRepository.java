package eg.gov.health.platform.notify;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface NotificationAttemptRepository extends JpaRepository<NotificationAttempt, UUID> {
  List<NotificationAttempt> findByNotificationIdOrderByCreatedAtAsc(UUID notificationId);
}
