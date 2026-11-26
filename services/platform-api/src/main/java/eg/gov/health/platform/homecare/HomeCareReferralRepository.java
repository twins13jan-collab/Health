package eg.gov.health.platform.homecare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface HomeCareReferralRepository extends JpaRepository<HomeCareReferral, UUID> {
  List<HomeCareReferral> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
