package eg.gov.health.platform.majorincidents;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface PublicHealthMessageRepository extends JpaRepository<PublicHealthMessage, UUID> {
}
