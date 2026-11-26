package eg.gov.health.platform.devicevigilance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface FieldSafetyActionRepository extends JpaRepository<FieldSafetyAction, UUID> {
  java.util.List<FieldSafetyAction> findByReportId(UUID reportId);
}
