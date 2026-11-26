package eg.gov.health.platform.rehab;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface RehabReferralRepository extends JpaRepository<RehabReferral, UUID> {
  List<RehabReferral> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
