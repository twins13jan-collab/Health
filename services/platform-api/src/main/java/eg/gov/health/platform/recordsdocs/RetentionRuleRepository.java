package eg.gov.health.platform.recordsdocs;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface RetentionRuleRepository extends JpaRepository<RetentionRule, UUID> {
  Optional<RetentionRule> findByFacilityIdAndDocType(UUID facilityId, String docType);
}
