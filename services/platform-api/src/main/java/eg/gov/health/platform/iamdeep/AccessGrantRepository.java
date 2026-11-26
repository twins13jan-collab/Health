package eg.gov.health.platform.iamdeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface AccessGrantRepository extends JpaRepository<AccessGrant, UUID> {}
