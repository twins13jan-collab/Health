package eg.gov.health.platform.integrations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, UUID> {
  List<DeliveryAttempt> findByMessageRefOrderByAttemptNoAsc(UUID messageRef);
}
