package eg.gov.health.platform.longitudinal;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PatientIdentityRepository extends JpaRepository<PatientIdentity, UUID> {
  List<PatientIdentity> findByPatientId(UUID patientId);
}
