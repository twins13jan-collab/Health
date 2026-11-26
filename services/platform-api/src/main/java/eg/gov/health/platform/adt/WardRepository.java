package eg.gov.health.platform.adt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WardRepository extends JpaRepository<Ward, UUID> {
  List<Ward> findByFacilityIdAndActiveTrueOrderByNameArAsc(UUID facilityId);
  List<Ward> findByFacilityIdOrderByNameArAsc(UUID facilityId);
  List<Ward> findByActiveTrue();
}
