package eg.gov.health.platform.institutionalrisk;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface RiskTreatmentRepository extends JpaRepository<RiskTreatment, UUID> {
  java.util.List<RiskTreatment> findByRiskId(UUID riskId);
}
