package eg.gov.health.platform.emergency;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface AmbulanceUnitRepository extends JpaRepository<AmbulanceUnit, UUID> {
  List<AmbulanceUnit> findByStatus(String status);
  Optional<AmbulanceUnit> findByUnitCode(String unitCode);
}
