package eg.gov.health.platform.servicedesk;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface KbArticleRepository extends JpaRepository<KbArticle, UUID> {}
