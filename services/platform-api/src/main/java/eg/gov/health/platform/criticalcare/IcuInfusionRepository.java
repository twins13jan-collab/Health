package eg.gov.health.platform.criticalcare;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface IcuInfusionRepository extends JpaRepository<IcuInfusion, UUID> {
  List<IcuInfusion> findByStayIdOrderByStartedAtDesc(UUID stayId);
}
