package eg.gov.health.platform.partnerships;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PartnerAccessGrantRepository extends JpaRepository<PartnerAccessGrant, UUID> {
  java.util.List<PartnerAccessGrant> findByPartnerId(UUID partnerId);
}
