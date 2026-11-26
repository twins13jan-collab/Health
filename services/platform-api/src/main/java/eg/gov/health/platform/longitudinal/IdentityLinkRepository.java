package eg.gov.health.platform.longitudinal;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface IdentityLinkRepository extends JpaRepository<IdentityLink, UUID> {
  List<IdentityLink> findByIdentityIdOrderByCreatedAtAsc(UUID identityId);
}
