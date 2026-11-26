package eg.gov.health.platform.infectioncontrol;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface HaiSurveillanceRepository extends JpaRepository<HaiSurveillance, UUID> {}
