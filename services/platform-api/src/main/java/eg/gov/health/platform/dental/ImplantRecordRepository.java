package eg.gov.health.platform.dental;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ImplantRecordRepository extends JpaRepository<ImplantRecord, UUID> {
  List<ImplantRecord> findByLotNumber(String lotNumber);
}
