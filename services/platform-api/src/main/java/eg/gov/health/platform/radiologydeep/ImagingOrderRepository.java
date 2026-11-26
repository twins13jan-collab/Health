package eg.gov.health.platform.radiologydeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ImagingOrderRepository extends JpaRepository<ImagingOrder, UUID> {
}
