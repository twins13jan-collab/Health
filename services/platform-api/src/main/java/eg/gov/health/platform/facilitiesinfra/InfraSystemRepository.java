package eg.gov.health.platform.facilitiesinfra;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface InfraSystemRepository extends JpaRepository<InfraSystem, UUID> {
}
