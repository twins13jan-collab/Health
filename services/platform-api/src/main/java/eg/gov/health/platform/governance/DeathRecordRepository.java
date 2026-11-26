package eg.gov.health.platform.governance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DeathRecordRepository extends JpaRepository<DeathRecord, UUID> {
  Optional<DeathRecord> findByPatientId(UUID patientId);
  List<DeathRecord> findByFacilityIdOrderByDiedAtDesc(UUID facilityId);
}
