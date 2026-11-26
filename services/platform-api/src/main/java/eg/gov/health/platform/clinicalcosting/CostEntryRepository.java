package eg.gov.health.platform.clinicalcosting;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CostEntryRepository extends JpaRepository<CostEntry, UUID> {
  java.util.List<CostEntry> findByCaseId(UUID caseId);
}
