package eg.gov.health.platform.workforce;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface ShiftRepository extends JpaRepository<Shift, UUID> {
  List<Shift> findByStaffId(UUID staffId);
  List<Shift> findByFacilityIdOrderByStartsAtAsc(UUID facilityId);
}
