package eg.gov.health.platform.devicevigilance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface VigilanceAssessmentRepository extends JpaRepository<VigilanceAssessment, UUID> {
  java.util.Optional<VigilanceAssessment> findByReportId(UUID reportId);
}
