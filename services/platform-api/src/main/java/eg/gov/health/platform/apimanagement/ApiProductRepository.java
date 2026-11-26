package eg.gov.health.platform.apimanagement;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ApiProductRepository extends JpaRepository<ApiProduct, UUID> {
}
