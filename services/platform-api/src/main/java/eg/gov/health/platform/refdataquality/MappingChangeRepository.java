package eg.gov.health.platform.refdataquality;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface MappingChangeRepository extends JpaRepository<MappingChange, UUID> {}
