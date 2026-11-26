package eg.gov.health.platform.organbank;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface OrganDiscardRecordRepository extends JpaRepository<OrganDiscardRecord, UUID> {
}
