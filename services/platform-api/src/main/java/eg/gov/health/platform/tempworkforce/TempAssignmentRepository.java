package eg.gov.health.platform.tempworkforce;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TempAssignmentRepository extends JpaRepository<TempAssignment, UUID> {
  java.util.List<TempAssignment> findByProviderIdAndStatusIn(UUID providerId, java.util.List<String> statuses);
}
