package eg.gov.health.platform.neonatal;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface NicuStayRepository extends JpaRepository<NicuStay, UUID> {
  List<NicuStay> findByNewbornIdOrderByStartedAtDesc(UUID newbornId);
  List<NicuStay> findByFacilityIdAndStatus(UUID facilityId, String status);
}
