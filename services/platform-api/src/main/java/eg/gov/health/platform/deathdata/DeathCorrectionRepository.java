package eg.gov.health.platform.deathdata;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DeathCorrectionRepository extends JpaRepository<DeathCorrection, UUID> {}
