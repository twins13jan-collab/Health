package eg.gov.health.platform.recalls;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import java.util.UUID;
public interface DrugCatalogItemRepository extends JpaRepository<DrugCatalogItem, UUID> {
  List<DrugCatalogItem> findByGenericNameContainingIgnoreCase(String name);
}
