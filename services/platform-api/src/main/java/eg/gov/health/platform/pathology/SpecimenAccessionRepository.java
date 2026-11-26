package eg.gov.health.platform.pathology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SpecimenAccessionRepository extends JpaRepository<SpecimenAccession, UUID> {
  List<SpecimenAccession> findByOrderIdOrderByReceivedAtAsc(UUID orderId);
}
