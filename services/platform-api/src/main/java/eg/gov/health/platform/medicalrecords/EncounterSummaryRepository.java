package eg.gov.health.platform.medicalrecords;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface EncounterSummaryRepository extends JpaRepository<EncounterSummary, UUID> {
  Optional<EncounterSummary> findByEncounterId(UUID encounterId);
}
