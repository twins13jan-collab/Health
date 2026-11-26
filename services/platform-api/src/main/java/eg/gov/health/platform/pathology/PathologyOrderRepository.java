package eg.gov.health.platform.pathology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface PathologyOrderRepository extends JpaRepository<PathologyOrder, UUID> {
  List<PathologyOrder> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
