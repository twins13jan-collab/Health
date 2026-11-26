package eg.gov.health.platform.aiadmin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AiProviderSettingsRepository extends JpaRepository<AiProviderSettings, String> {
  Optional<AiProviderSettings> findByActiveTrue();
}
