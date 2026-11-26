package eg.gov.health.platform.porterage;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface RoomTurnoverTaskRepository extends JpaRepository<RoomTurnoverTask, UUID> {  java.util.List<RoomTurnoverTask> findByFacilityIdOrderByDischargedAtAsc(java.util.UUID facilityId);
}
