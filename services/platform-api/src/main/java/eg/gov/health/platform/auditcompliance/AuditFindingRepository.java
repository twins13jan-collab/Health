package eg.gov.health.platform.auditcompliance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AuditFindingRepository extends JpaRepository<AuditFinding, UUID> {}
