package eg.gov.health.platform.notify;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {
  List<NotificationOutbox> findByStatusOrderByCreatedAtAsc(String status);
  List<NotificationOutbox> findByRecipientRefAndChannelOrderByCreatedAtDesc(String recipientRef, String channel);
}
