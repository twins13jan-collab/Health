package eg.gov.health.platform.nutrition;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AdministrationRecordRepository extends JpaRepository<AdministrationRecord, UUID> {
  List<AdministrationRecord> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
