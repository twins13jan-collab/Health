package eg.gov.health.platform.childhealth;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AefiReportRepository extends JpaRepository<AefiReport, UUID> {
  java.util.List<AefiReport> findByDoseId(UUID doseId);
}
