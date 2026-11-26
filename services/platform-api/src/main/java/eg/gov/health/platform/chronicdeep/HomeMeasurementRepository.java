package eg.gov.health.platform.chronicdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface HomeMeasurementRepository extends JpaRepository<HomeMeasurement, UUID> {
  java.util.List<HomeMeasurement> findByEnrollmentIdOrderByCreatedAtDesc(UUID enrollmentId);
}
