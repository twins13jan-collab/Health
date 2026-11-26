package eg.gov.health.platform.clinical;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ProblemListRepository extends JpaRepository<ProblemListItem, UUID> {
  List<ProblemListItem> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
