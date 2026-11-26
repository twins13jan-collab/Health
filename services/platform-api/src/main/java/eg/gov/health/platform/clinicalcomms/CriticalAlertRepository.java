package eg.gov.health.platform.clinicalcomms;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CriticalAlertRepository extends JpaRepository<CriticalAlert, UUID> {}
