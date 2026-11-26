package eg.gov.health.platform.diseaseregistries;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface RegistryCaseRepository extends JpaRepository<RegistryCase, UUID> {
  java.util.List<RegistryCase> findByRegistryId(UUID registryId);
}
