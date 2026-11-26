package eg.gov.health.platform.facilityestablishment;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CommissioningCheckRepository extends JpaRepository<CommissioningCheck, UUID> {
  java.util.List<CommissioningCheck> findByProjectId(UUID projectId);
}
