package eg.gov.health.platform.porthealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface QuarantineOrderRepository extends JpaRepository<QuarantineOrder, UUID> {}
