package eg.gov.health.platform.endoscopy;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PreProcedureChecklistRepository extends JpaRepository<PreProcedureChecklist, UUID> {
  List<PreProcedureChecklist> findByRequestIdOrderByCreatedAtDesc(UUID requestId);
}
