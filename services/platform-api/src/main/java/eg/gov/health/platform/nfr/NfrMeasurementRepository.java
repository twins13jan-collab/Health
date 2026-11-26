package eg.gov.health.platform.nfr;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface NfrMeasurementRepository extends JpaRepository<NfrMeasurement, UUID> {
  java.util.List<NfrMeasurement> findByRequirementIdOrderByMeasuredOnDesc(UUID requirementId);
}
