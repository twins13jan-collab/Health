package eg.gov.health.platform.wounds;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface BurnResuscitationRecordRepository extends JpaRepository<BurnResuscitationRecord, UUID> {
  List<BurnResuscitationRecord> findByCaseIdOrderByCreatedAtDesc(UUID caseId);
}
