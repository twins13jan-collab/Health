package eg.gov.health.platform.legalcompliance;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface LegalRequirementRepository extends JpaRepository<LegalRequirement, UUID> {}
