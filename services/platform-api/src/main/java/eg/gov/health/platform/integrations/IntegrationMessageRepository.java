package eg.gov.health.platform.integrations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface IntegrationMessageRepository extends JpaRepository<IntegrationMessage, UUID> {
  Optional<IntegrationMessage> findByMessageId(String messageId);
  List<IntegrationMessage> findByStatusOrderByCreatedAtAsc(String status);
}
