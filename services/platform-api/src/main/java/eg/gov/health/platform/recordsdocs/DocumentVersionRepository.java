package eg.gov.health.platform.recordsdocs;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {
  List<DocumentVersion> findByDocumentIdOrderByVersionAsc(UUID documentId);
}
