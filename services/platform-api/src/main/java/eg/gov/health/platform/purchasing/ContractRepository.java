package eg.gov.health.platform.purchasing;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ContractRepository extends JpaRepository<Contract, UUID> {
  List<Contract> findByPoIdOrderByVersionDesc(UUID poId);
}
