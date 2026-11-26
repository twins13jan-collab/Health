package eg.gov.health.platform.softwarelifecycle;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SoftwareReleaseRepository extends JpaRepository<SoftwareRelease, UUID> {}
