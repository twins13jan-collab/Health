package eg.gov.health.platform.telemedicine;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TeleSessionRepository extends JpaRepository<TeleSession, UUID> {}
