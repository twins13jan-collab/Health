package eg.gov.health.platform.longitudinal;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ClinicalRecordLinkRepository extends JpaRepository<ClinicalRecordLink, UUID> {
  List<ClinicalRecordLink> findByIdentityIdOrderByCreatedAtAsc(UUID identityId);
  long countByIdentityId(UUID identityId);
}
