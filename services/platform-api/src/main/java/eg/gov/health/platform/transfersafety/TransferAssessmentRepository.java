package eg.gov.health.platform.transfersafety;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TransferAssessmentRepository extends JpaRepository<TransferAssessment, UUID> {}
