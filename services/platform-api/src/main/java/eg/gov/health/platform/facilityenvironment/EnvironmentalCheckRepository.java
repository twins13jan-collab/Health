package eg.gov.health.platform.facilityenvironment;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface EnvironmentalCheckRepository extends JpaRepository<EnvironmentalCheck, UUID> {}
