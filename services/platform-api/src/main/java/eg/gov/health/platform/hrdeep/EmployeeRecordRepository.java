package eg.gov.health.platform.hrdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface EmployeeRecordRepository extends JpaRepository<EmployeeRecord, UUID> {
  java.util.List<EmployeeRecord> findByFacilityId(UUID facilityId);
}
