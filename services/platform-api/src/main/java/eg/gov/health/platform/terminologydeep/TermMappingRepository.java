package eg.gov.health.platform.terminologydeep;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface TermMappingRepository extends JpaRepository<TermMapping, UUID> {}
