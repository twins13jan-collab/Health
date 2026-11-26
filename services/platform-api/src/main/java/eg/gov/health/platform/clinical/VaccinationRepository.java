package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface VaccinationRepository extends JpaRepository<Vaccination, UUID> {
  List<Vaccination> findByPatientId(UUID patientId);
}
