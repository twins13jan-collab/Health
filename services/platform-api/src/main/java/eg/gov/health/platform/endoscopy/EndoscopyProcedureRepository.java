package eg.gov.health.platform.endoscopy;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface EndoscopyProcedureRepository extends JpaRepository<EndoscopyProcedure, UUID> {
  List<EndoscopyProcedure> findByScopeIdOrderByCreatedAtDesc(UUID scopeId);
}
