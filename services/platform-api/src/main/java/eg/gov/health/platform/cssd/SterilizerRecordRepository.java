package eg.gov.health.platform.cssd;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SterilizerRecordRepository extends JpaRepository<SterilizerRecord, UUID> {
  java.util.List<SterilizerRecord> findByFacilityIdOrderBySterilizerCodeAsc(UUID facilityId);
}
