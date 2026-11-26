package eg.gov.health.platform.meddelivery;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, UUID> {
}
