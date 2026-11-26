package eg.gov.health.platform.accreditation;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ServiceRestrictionRepository extends JpaRepository<ServiceRestriction, UUID> {}
