package eg.gov.health.platform.toxicology;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ExposureEventRepository extends JpaRepository<ExposureEvent, UUID> {}
