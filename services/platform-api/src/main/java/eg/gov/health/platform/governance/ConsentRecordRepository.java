package eg.gov.health.platform.governance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {
  List<ConsentRecord> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
  List<ConsentRecord> findByPatientIdAndPurposeAndRecipientAndDataScopeAndStatus(UUID patientId, String purpose, String recipient, String dataScope, String status);
}
