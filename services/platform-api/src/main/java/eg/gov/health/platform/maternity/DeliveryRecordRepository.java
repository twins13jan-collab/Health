package eg.gov.health.platform.maternity;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DeliveryRecordRepository extends JpaRepository<DeliveryRecord, UUID> {
  List<DeliveryRecord> findByPregnancyIdOrderByDeliveredAtDesc(UUID pregnancyId);
}
