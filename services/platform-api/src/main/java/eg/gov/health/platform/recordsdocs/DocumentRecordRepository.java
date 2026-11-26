package eg.gov.health.platform.recordsdocs;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, UUID> {}
