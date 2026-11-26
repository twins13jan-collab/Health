package eg.gov.health.platform.surgeryops;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OperatingRoomRepository extends JpaRepository<OperatingRoom, UUID> {
  List<OperatingRoom> findByFacilityIdOrderByNameArAsc(UUID facilityId);
}
