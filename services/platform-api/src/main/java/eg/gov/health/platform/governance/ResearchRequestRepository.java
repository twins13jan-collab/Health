package eg.gov.health.platform.governance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ResearchRequestRepository extends JpaRepository<ResearchRequest, UUID> {
  List<ResearchRequest> findByStatusOrderByCreatedAtAsc(String status);
}
