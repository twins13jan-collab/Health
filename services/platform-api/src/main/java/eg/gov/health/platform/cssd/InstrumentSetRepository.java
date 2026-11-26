package eg.gov.health.platform.cssd;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface InstrumentSetRepository extends JpaRepository<InstrumentSet, UUID> {
  java.util.List<InstrumentSet> findByFacilityIdOrderByUpdatedAtDesc(UUID facilityId);
}
