package eg.gov.health.platform.recordsdocs;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SignatureRecordRepository extends JpaRepository<SignatureRecord, UUID> {
  List<SignatureRecord> findByDocumentIdOrderByCreatedAtDesc(UUID documentId);
}
