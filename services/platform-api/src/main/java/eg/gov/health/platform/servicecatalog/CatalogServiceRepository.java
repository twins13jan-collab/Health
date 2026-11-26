package eg.gov.health.platform.servicecatalog;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface CatalogServiceRepository extends JpaRepository<CatalogService, UUID> {
  java.util.List<CatalogService> findByCode(String code);
}
