package eg.gov.health.platform.servicecatalog;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CatalogChangeRepository extends JpaRepository<CatalogChange, UUID> {
}
