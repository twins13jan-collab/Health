package eg.gov.health.platform.homecare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface RemoteObservationRepository extends JpaRepository<RemoteObservation, UUID> {
  List<RemoteObservation> findByPatientIdOrderBySourceTimeDesc(UUID patientId);
}
