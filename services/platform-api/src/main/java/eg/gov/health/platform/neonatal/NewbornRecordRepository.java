package eg.gov.health.platform.neonatal;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface NewbornRecordRepository extends JpaRepository<NewbornRecord, UUID> {
  List<NewbornRecord> findByMotherPatientIdOrderByBornAtDesc(UUID motherPatientId);
  List<NewbornRecord> findByDeliveryId(UUID deliveryId);
  List<NewbornRecord> findByPregnancyId(UUID pregnancyId);
}
