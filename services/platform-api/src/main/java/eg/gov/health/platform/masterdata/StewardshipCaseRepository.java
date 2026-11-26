package eg.gov.health.platform.masterdata;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface StewardshipCaseRepository extends JpaRepository<StewardshipCase, UUID> {
  List<StewardshipCase> findByTargetRefAndChangeTypeAndDecision(String targetRef, String changeType, String decision);
}
