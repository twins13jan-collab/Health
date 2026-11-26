package eg.gov.health.platform.dental;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DentalImageRepository extends JpaRepository<DentalImage, UUID> {
  List<DentalImage> findByPatientIdOrderByTakenAtDesc(UUID patientId);
}
