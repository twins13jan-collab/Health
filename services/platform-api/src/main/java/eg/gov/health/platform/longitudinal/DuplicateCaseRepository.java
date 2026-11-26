package eg.gov.health.platform.longitudinal;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DuplicateCaseRepository extends JpaRepository<DuplicateCase, UUID> {
  List<DuplicateCase> findByStatusOrderByCreatedAtAsc(String status);
}
