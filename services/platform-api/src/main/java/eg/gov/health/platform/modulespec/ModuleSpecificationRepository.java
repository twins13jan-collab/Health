package eg.gov.health.platform.modulespec;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ModuleSpecificationRepository extends JpaRepository<ModuleSpecification, UUID> {}
