package eg.gov.health.platform.cycleverification;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CycleVerificationRepository extends JpaRepository<CycleVerification, UUID> {}
