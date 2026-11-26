package eg.gov.health.platform.accreditation;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface LicenceFileRepository extends JpaRepository<LicenceFile, UUID> {}
