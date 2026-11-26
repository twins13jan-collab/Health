package eg.gov.health.platform.hrdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, UUID> {
  java.util.List<TrainingRecord> findByFacilityId(UUID facilityId);
}
