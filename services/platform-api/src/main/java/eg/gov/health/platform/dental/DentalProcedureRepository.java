package eg.gov.health.platform.dental;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DentalProcedureRepository extends JpaRepository<DentalProcedure, UUID> {
  List<DentalProcedure> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
