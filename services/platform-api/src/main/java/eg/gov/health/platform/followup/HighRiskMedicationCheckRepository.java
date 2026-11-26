package eg.gov.health.platform.followup;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface HighRiskMedicationCheckRepository extends JpaRepository<HighRiskMedicationCheck, UUID> {
}
