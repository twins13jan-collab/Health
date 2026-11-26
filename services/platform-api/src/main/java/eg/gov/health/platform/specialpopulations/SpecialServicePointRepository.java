package eg.gov.health.platform.specialpopulations;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SpecialServicePointRepository extends JpaRepository<SpecialServicePoint, UUID> {}
