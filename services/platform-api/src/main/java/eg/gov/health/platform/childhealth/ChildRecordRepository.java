package eg.gov.health.platform.childhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ChildRecordRepository extends JpaRepository<ChildRecord, UUID> {
}
