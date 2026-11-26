package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface EncounterRepository extends JpaRepository<Encounter, UUID> {
  List<Encounter> findByFacilityIdOrderByArrivedAtDesc(UUID facilityId);
  List<Encounter> findByPatientIdOrderByArrivedAtDesc(UUID patientId);
}
