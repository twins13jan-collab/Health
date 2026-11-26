package eg.gov.health.platform.cssd;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SetUsageLinkRepository extends JpaRepository<SetUsageLink, UUID> {
  java.util.List<SetUsageLink> findByFacilityIdOrderByUsedAtDesc(UUID facilityId);
}
