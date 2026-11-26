package eg.gov.health.platform.deathdata;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DeathRegistrationRepository extends JpaRepository<DeathRegistration, UUID> {}
