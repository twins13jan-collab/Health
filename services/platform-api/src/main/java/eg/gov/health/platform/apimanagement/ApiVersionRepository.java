package eg.gov.health.platform.apimanagement;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ApiVersionRepository extends JpaRepository<ApiVersion, UUID> {
  java.util.List<ApiVersion> findByProductId(UUID productId);
}
