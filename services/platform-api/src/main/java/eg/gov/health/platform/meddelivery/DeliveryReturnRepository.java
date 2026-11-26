package eg.gov.health.platform.meddelivery;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DeliveryReturnRepository extends JpaRepository<DeliveryReturn, UUID> {
}
