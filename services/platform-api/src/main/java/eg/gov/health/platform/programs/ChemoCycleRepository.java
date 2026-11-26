package eg.gov.health.platform.programs;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ChemoCycleRepository extends JpaRepository<ChemoCycle, UUID> {
  List<ChemoCycle> findByPatientIdOrderByCycleNumberAsc(UUID patientId);
}
