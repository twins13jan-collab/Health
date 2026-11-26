package eg.gov.health.platform.medcontent;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface EducationContentItemRepository extends JpaRepository<EducationContentItem, UUID> {}
