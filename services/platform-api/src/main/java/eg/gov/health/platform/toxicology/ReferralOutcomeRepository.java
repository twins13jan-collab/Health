package eg.gov.health.platform.toxicology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ReferralOutcomeRepository extends JpaRepository<ReferralOutcome, UUID> {
  List<ReferralOutcome> findByCaseIdOrderByCreatedAtDesc(UUID caseId);
}
