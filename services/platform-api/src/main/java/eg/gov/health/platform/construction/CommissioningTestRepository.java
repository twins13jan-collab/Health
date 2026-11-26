package eg.gov.health.platform.construction;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CommissioningTestRepository extends JpaRepository<CommissioningTest, UUID> {
  java.util.List<CommissioningTest> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
