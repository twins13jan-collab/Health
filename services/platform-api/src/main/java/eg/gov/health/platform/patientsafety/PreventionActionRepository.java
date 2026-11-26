package eg.gov.health.platform.patientsafety;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PreventionActionRepository extends JpaRepository<PreventionAction, UUID> {
  java.util.List<PreventionAction> findByAnalysisId(UUID analysisId);
}
