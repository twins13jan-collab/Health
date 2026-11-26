package eg.gov.health.platform.sensory;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface EntProcedureRepository extends JpaRepository<EntProcedure, UUID> {
  List<EntProcedure> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
