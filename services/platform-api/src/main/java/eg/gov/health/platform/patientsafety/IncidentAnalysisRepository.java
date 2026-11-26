package eg.gov.health.platform.patientsafety;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface IncidentAnalysisRepository extends JpaRepository<IncidentAnalysis, UUID> {
  java.util.List<IncidentAnalysis> findByIncidentId(UUID incidentId);
}
