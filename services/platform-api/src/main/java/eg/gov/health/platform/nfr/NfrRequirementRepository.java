package eg.gov.health.platform.nfr;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface NfrRequirementRepository extends JpaRepository<NfrRequirement, UUID> {
  java.util.List<NfrRequirement> findByServiceRef(String serviceRef);
}
