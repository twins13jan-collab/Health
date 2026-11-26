package eg.gov.health.platform.sensory;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AssistiveDeviceRepository extends JpaRepository<AssistiveDevice, UUID> {
  List<AssistiveDevice> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
