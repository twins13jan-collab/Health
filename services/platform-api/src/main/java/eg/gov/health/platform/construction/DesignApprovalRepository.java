package eg.gov.health.platform.construction;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DesignApprovalRepository extends JpaRepository<DesignApproval, UUID> {
  boolean existsByProjectIdAndDisciplineAndApprovedTrue(UUID projectId, String discipline);
}
