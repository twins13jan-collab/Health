package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface TeleconsultationRepository extends JpaRepository<Teleconsultation, UUID> {
  List<Teleconsultation> findByPatientIdOrderByScheduledAtAsc(UUID patientId);
}
