package eg.gov.health.platform.dental;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DentalChartRepository extends JpaRepository<DentalChart, UUID> {
  List<DentalChart> findByPatientIdAndStatusOrderByVersionDesc(UUID patientId, String status);
}
