package eg.gov.health.platform.criticalcare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface EscalationEventRepository extends JpaRepository<EscalationEvent, UUID> {
  List<EscalationEvent> findByStayIdOrderByCalledAtDesc(UUID stayId);
}
