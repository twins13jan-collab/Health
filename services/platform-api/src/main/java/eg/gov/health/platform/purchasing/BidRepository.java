package eg.gov.health.platform.purchasing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface BidRepository extends JpaRepository<Bid, UUID> {
  List<Bid> findByTenderIdOrderByAmountAsc(UUID tenderId);
}
