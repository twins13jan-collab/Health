package eg.gov.health.platform.diagnostics;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface RadiologyOrderRepository extends JpaRepository<RadiologyOrder, UUID> {
  List<RadiologyOrder> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  List<RadiologyOrder> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
