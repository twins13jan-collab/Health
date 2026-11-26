package eg.gov.health.platform.patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate; import java.util.*; import java.util.UUID;
public interface PatientRepository extends JpaRepository<Patient, UUID> {
  Optional<Patient> findByNationalId(String nationalId);
  List<Patient> findByFullNameAndBirthDate(String fullName, LocalDate birthDate);
  List<Patient> findByFacilityId(UUID facilityId);
}
