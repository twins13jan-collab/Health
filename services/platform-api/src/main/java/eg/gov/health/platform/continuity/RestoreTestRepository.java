package eg.gov.health.platform.continuity;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface RestoreTestRepository extends JpaRepository<RestoreTest, UUID> {
  java.util.List<RestoreTest> findByServiceIdOrderByTestedAtDesc(UUID serviceId);
}
