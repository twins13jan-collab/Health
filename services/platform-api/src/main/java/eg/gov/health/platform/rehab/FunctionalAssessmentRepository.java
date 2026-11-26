package eg.gov.health.platform.rehab;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface FunctionalAssessmentRepository extends JpaRepository<FunctionalAssessment, UUID> {
  List<FunctionalAssessment> findByReferralIdOrderByAssessedAtAsc(UUID referralId);
}
