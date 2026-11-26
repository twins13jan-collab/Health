package eg.gov.health.platform.porterage;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface LaundryBatchRepository extends JpaRepository<LaundryBatch, UUID> {  java.util.List<LaundryBatch> findByFacilityIdOrderByCreatedAtDesc(java.util.UUID facilityId);
}
