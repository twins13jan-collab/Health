package eg.gov.health.platform.integrations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface InterfaceContractRepository extends JpaRepository<InterfaceContract, UUID> {
  List<InterfaceContract> findByEndpointIdOrderByCreatedAtAsc(UUID endpointId);
}
