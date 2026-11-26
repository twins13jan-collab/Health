package eg.gov.health.platform.meddelivery;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface MedDeliveryAttemptRepository extends JpaRepository<MedDeliveryAttempt, UUID> {
  long countByOrderIdAndOutcome(UUID orderId, String outcome); long countByOrderId(UUID orderId);
}
