package eg.gov.health.platform.organbank;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface CustodyChainEventRepository extends JpaRepository<CustodyChainEvent, UUID> {
  java.util.List<CustodyChainEvent> findByDonationIdOrderByEventAtAsc(UUID donationId);
}
