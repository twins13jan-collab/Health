package eg.gov.health.platform.opendata;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface OpenDatasetRepository extends JpaRepository<OpenDataset, UUID> {}
