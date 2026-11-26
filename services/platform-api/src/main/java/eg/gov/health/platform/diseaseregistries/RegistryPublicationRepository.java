package eg.gov.health.platform.diseaseregistries;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface RegistryPublicationRepository extends JpaRepository<RegistryPublication, UUID> {
}
