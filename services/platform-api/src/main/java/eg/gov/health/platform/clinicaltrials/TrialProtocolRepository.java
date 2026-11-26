package eg.gov.health.platform.clinicaltrials;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TrialProtocolRepository extends JpaRepository<TrialProtocol, UUID> {}
