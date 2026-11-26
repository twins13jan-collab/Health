package eg.gov.health.platform.construction;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ContractChangeOrderRepository extends JpaRepository<ContractChangeOrder, UUID> {
}
