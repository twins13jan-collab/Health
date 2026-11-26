package eg.gov.health.platform.ethicscommittees;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CommitteeRepository extends JpaRepository<Committee, UUID> {
}
