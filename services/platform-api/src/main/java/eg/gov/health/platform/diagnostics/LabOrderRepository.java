package eg.gov.health.platform.diagnostics;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface LabOrderRepository extends JpaRepository<LabOrder, UUID> {
  List<LabOrder> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  List<LabOrder> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
