package eg.gov.health.platform.masterdata;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface DataQualityRuleRepository extends JpaRepository<DataQualityRule, UUID> {}
