package eg.gov.health.platform.digitalquality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface InterfaceUserTestRepository extends JpaRepository<InterfaceUserTest, UUID> {
}
