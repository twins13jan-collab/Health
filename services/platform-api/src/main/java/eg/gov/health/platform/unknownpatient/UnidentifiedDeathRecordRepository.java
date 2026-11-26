package eg.gov.health.platform.unknownpatient;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface UnidentifiedDeathRecordRepository extends JpaRepository<UnidentifiedDeathRecord, UUID> {
}
