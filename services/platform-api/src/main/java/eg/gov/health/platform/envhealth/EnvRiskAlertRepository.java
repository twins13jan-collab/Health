package eg.gov.health.platform.envhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface EnvRiskAlertRepository extends JpaRepository<EnvRiskAlert, UUID> {}
