package eg.gov.health.platform.medicalrecords;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface EncounterCodingRepository extends JpaRepository<EncounterCoding, UUID> {
  Optional<EncounterCoding> findByEncounterId(UUID encounterId);
}
