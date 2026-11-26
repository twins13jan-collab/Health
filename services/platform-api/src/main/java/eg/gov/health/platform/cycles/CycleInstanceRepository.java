package eg.gov.health.platform.cycles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*; import java.util.UUID;
public interface CycleInstanceRepository extends JpaRepository<CycleInstance, UUID> {
  List<CycleInstance> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId);
  Optional<CycleInstance> findByFacilityIdAndIdempotencyKey(UUID facilityId, String idempotencyKey);
  long countByFacilityIdAndStatus(UUID facilityId, String status);
  List<CycleInstance> findByFacilityIdAndSlaDueAtBeforeAndStatusNotIn(UUID facilityId, java.time.OffsetDateTime when, List<String> statuses);
}
