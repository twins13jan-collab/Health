package eg.gov.health.platform.surgeryops;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AnaesthesiaRecordRepository extends JpaRepository<AnaesthesiaRecord, UUID> {
  Optional<AnaesthesiaRecord> findByCaseId(UUID caseId);
}
