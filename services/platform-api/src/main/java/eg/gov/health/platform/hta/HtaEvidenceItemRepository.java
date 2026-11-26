package eg.gov.health.platform.hta;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface HtaEvidenceItemRepository extends JpaRepository<HtaEvidenceItem, UUID> {
}
