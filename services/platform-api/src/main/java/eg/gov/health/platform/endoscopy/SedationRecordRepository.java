package eg.gov.health.platform.endoscopy;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface SedationRecordRepository extends JpaRepository<SedationRecord, UUID> {
  List<SedationRecord> findByProcedureIdOrderByCreatedAtDesc(UUID procedureId);
}
