package eg.gov.health.platform.criticalcare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface IcuObservationRepository extends JpaRepository<IcuObservation, UUID> {
  List<IcuObservation> findByStayIdOrderBySourceTimeDesc(UUID stayId);
}
