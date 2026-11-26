package eg.gov.health.platform.recordsdocs;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DocumentAccessEventRepository extends JpaRepository<DocumentAccessEvent, UUID> {
  List<DocumentAccessEvent> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);
}
