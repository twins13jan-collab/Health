package eg.gov.health.platform.partnerships;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PartnerContractRepository extends JpaRepository<PartnerContract, UUID> {
  java.util.List<PartnerContract> findByPartnerId(UUID partnerId);
}
