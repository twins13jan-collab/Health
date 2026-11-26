package eg.gov.health.platform.supplychaindeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SupplySourceRepository extends JpaRepository<SupplySource, UUID> {
  java.util.List<SupplySource> findByItemRef(String itemRef);
}
