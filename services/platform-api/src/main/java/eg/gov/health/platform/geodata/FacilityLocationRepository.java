package eg.gov.health.platform.geodata;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface FacilityLocationRepository extends JpaRepository<FacilityLocation, UUID> {}
