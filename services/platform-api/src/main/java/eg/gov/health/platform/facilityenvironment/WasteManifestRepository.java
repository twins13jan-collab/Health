package eg.gov.health.platform.facilityenvironment;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface WasteManifestRepository extends JpaRepository<WasteManifest, UUID> {}
