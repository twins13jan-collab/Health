package eg.gov.health.platform.integrations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ReconciliationCaseRepository extends JpaRepository<ReconciliationCase, UUID> {
  List<ReconciliationCase> findByMessageRef(UUID messageRef);
}
